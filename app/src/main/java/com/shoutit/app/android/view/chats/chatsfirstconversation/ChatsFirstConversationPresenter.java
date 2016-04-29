package com.shoutit.app.android.view.chats.chatsfirstconversation;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pusher.client.channel.PresenceChannel;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ConversationProfile;
import com.shoutit.app.android.api.model.Message;
import com.shoutit.app.android.api.model.MessageAttachment;
import com.shoutit.app.android.api.model.PostMessage;
import com.shoutit.app.android.api.model.PusherMessage;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.PusherHelper;
import com.shoutit.app.android.view.chats.ChatsDelegate;
import com.shoutit.app.android.view.chats.message_models.TypingItem;
import com.shoutit.app.android.view.conversations.ConversationsUtils;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class ChatsFirstConversationPresenter {

    private String conversationId;
    private boolean conversationCreated;

    @NonNull
    private final ApiService mApiService;
    private final Scheduler mUiScheduler;
    private final Scheduler mNetworkScheduler;
    private final UserPreferences mUserPreferences;
    private final Resources mResources;
    private final Context mContext;
    private final PusherHelper mPusher;
    private final String mIdForCreation;
    private final ShoutsDao mShoutsDao;
    private final ProfilesDao mProfilesDao;
    private final boolean mIsShoutConversation;
    private FirstConversationListener mListener;
    private final CompositeSubscription mSubscribe = new CompositeSubscription();
    private final PublishSubject<PusherMessage> newMessagesSubject = PublishSubject.create();
    private final PublishSubject<Object> mRefreshTypingObservable = PublishSubject.create();
    private final BehaviorSubject<String> chatParticipantUsernameSubject = BehaviorSubject.create();
    private final Observable<String> calledPersonUsernameObservable;
    private PublishSubject<Object> mLocalAndPusherMessagesSubject;
    private final ChatsDelegate mChatsDelegate;

    @Inject
    public ChatsFirstConversationPresenter(boolean isShoutConversation,
                                           @NonNull ApiService apiService,
                                           @UiScheduler Scheduler uiScheduler,
                                           @NetworkScheduler Scheduler networkScheduler,
                                           final UserPreferences userPreferences,
                                           @ForActivity Resources resources,
                                           @ForActivity Context context,
                                           AmazonHelper amazonHelper,
                                           PusherHelper pusher,
                                           String idForCreation,
                                           ShoutsDao shoutsDao,
                                           ProfilesDao profilesDao) {
        mIsShoutConversation = isShoutConversation;
        mApiService = apiService;
        mUiScheduler = uiScheduler;
        mNetworkScheduler = networkScheduler;
        mUserPreferences = userPreferences;
        mResources = resources;
        mContext = context;
        mPusher = pusher;
        mIdForCreation = idForCreation;
        mShoutsDao = shoutsDao;
        mProfilesDao = profilesDao;

        calledPersonUsernameObservable = chatParticipantUsernameSubject
                .filter(Functions1.isNotNull())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String participantUsername) {
                        return !Objects.equal(userPreferences.getUser().getUsername(), participantUsername);
                    }
                });

        mChatsDelegate = new ChatsDelegate(pusher, uiScheduler, networkScheduler, apiService, resources, userPreferences, context, amazonHelper);
    }

    public void register(@NonNull FirstConversationListener listener) {
        final User user = mUserPreferences.getUser();
        assert user != null;
        mListener = listener;
        mListener.showDeleteMenu(false);
        mChatsDelegate.setListener(listener);

        subscribeToMessages();

        getConversationInfo(user);
    }

    private void getConversationInfo(User user) {
        if (mIsShoutConversation) {
            getShout(user);
        } else {
            getUser();
        }
    }

    private void getUser() {
        mSubscribe.add(mApiService.getUser(mIdForCreation)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        mListener.setChatToolbatInfo(ConversationsUtils.getChatWithString(
                                ImmutableList.of(new ConversationProfile(
                                        user.getId(),
                                        user.getName(),
                                        user.getUsername(),
                                        user.getType(),
                                        user.getImage())), mUserPreferences.getUser().getId()));
                    }
                }, getOnError()));
    }

    private void getShout(final User user) {
        mSubscribe.add(mShoutsDao.getShoutObservable(mIdForCreation)
                .observeOn(mUiScheduler)
                .compose(ResponseOrError.<Shout>onlySuccess())
                .subscribe(new Action1<Shout>() {
                    @Override
                    public void call(Shout about) {
                        final String title = about.getTitle();
                        final String thumbnail = Strings.emptyToNull(about.getThumbnail());
                        final String type = about.getType().equals(Shout.TYPE_OFFER) ? mContext.getString(R.string.chat_offer) : mContext.getString(R.string.chat_request);
                        final String price = PriceUtils.formatPriceWithCurrency(about.getPrice(), mResources, about.getCurrency());
                        final User profile = about.getProfile();
                        final String authorAndTime = profile.getName() + " - " + DateUtils.getRelativeTimeSpanString(mContext, about.getDatePublishedInMillis());
                        final String id = about.getId();

                        if (!Strings.isNullOrEmpty(id)) {
                            mListener.setAboutShoutData(title, thumbnail, type, price, authorAndTime, id);
                            mListener.setShoutToolbarInfo(title, ConversationsUtils.getChatWithString(
                                    ImmutableList.of(new ConversationProfile(
                                            profile.getId(),
                                            profile.getName(),
                                            profile.getUsername(),
                                            profile.getType(),
                                            profile.getImage())), user.getId()));
                        } else {
                            mListener.setShoutToolbarInfo(mContext.getString(R.string.chat_shout_chat), ConversationsUtils.getChatWithString(
                                    ImmutableList.of(new ConversationProfile(
                                            profile.getId(),
                                            profile.getName(),
                                            profile.getUsername(),
                                            profile.getType(),
                                            profile.getImage())), user.getId()));
                        }
                    }
                }, getOnError()));
    }

    private void subscribeToMessages() {
        final PresenceChannel presenceChannel = mChatsDelegate.getConversationChannel(conversationId);

        final Observable<PusherMessage> pusherMessageObservable = mChatsDelegate.getPusherMessageObservable(presenceChannel);

        final Observable<Boolean> isTyping = mChatsDelegate.getTypingObservable(presenceChannel);

        mLocalAndPusherMessagesSubject = PublishSubject.create();

        final Observable<List<PusherMessage>> localAndPusherMessages = mLocalAndPusherMessagesSubject.switchMap(
                new Func1<Object, Observable<PusherMessage>>() {
                    @Override
                    public Observable<PusherMessage> call(Object o) {
                        return pusherMessageObservable.mergeWith(newMessagesSubject);
                    }
                })
                .compose(mChatsDelegate.transformToScan());

        mSubscribe.add(Observable.combineLatest(
                localAndPusherMessages.map(new Func1<List<PusherMessage>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<PusherMessage> pusherMessages) {
                        return mChatsDelegate.transform(ImmutableList.copyOf(Iterables.transform(pusherMessages, new Function<PusherMessage, Message>() {
                            @Nullable
                            @Override
                            public Message apply(@Nullable PusherMessage message) {
                                assert message != null;
                                return new Message(
                                        conversationId, message.getProfile(),
                                        message.getId(),
                                        message.getText(),
                                        message.getAttachments(),
                                        message.getCreatedAt());
                            }
                        })));
                    }
                }), mRefreshTypingObservable.switchMap(new Func1<Object, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Object o) {
                        return isTyping;
                    }
                }), new Func2<List<BaseAdapterItem>, Boolean, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<BaseAdapterItem> baseAdapterItems, Boolean isTyping) {
                        if (isTyping) {
                            return ImmutableList.<BaseAdapterItem>builder()
                                    .addAll(baseAdapterItems)
                                    .add(new TypingItem())
                                    .build();
                        } else {
                            return baseAdapterItems;
                        }
                    }
                })
                .subscribe(new Action1<List<BaseAdapterItem>>() {
                    @Override
                    public void call(@NonNull List<BaseAdapterItem> baseAdapterItems) {
                        mChatsDelegate.messagesSuccess(baseAdapterItems, mListener);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mChatsDelegate.messagesError(throwable, mListener);
                    }
                }));
    }

    @NonNull
    private Action1<Throwable> getOnError() {
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mListener.error(throwable);
            }
        };
    }

    public void postTextMessage(@NonNull String text) {
        final PostMessage message = new PostMessage(text, ImmutableList.<MessageAttachment>of());
        mSubscribe.add(sendMessage(message)
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message messagesResponse) {
                        mChatsDelegate.postLocalMessage(messagesResponse, conversationId);
                    }
                }, getOnError()));
        ;
    }

    private Observable<Message> sendMessage(PostMessage message) {
        Observable<Message> observable;
        if (conversationCreated) {
            observable = mApiService.postMessage(conversationId, message)
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler);
        } else {
            if (mIsShoutConversation) {
                observable = mApiService.createShoutConversation(mIdForCreation, message)
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler);
            } else {
                observable = mApiService.createChatConversation(mIdForCreation, message)
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler);
            }
        }
        return observable.doOnNext(new Action1<Message>() {
            @Override
            public void call(Message message) {
                mListener.showDeleteMenu(true);
                conversationCreated = true;
                conversationId = message.getConversationId();
                if (mIsShoutConversation) {
                    mShoutsDao.getShoutDao(mIdForCreation).getRefreshObserver().onNext(new Object());
                } else {
                    mProfilesDao.getProfileDao(mIdForCreation).getRefreshSubject().onNext(new Object());
                }
                mRefreshTypingObservable.onNext(new Object());
                mLocalAndPusherMessagesSubject.onNext(new Object());
            }
        });
    }

    public void unregister() {
        mListener = null;
        mSubscribe.unsubscribe();
        mPusher.unsubscribeConversationChannel(conversationId);
        mChatsDelegate.removeListener();
    }

    public void addMedia(@NonNull String media, boolean isVideo) {
        mSubscribe.add(mChatsDelegate.addMedia(media, isVideo, new Func1<Video, Observable<Message>>() {
            @Override
            public Observable<Message> call(Video video) {
                final PostMessage message = new PostMessage(null, ImmutableList.of(new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_MEDIA, null, null, null, ImmutableList.of(video))));
                return sendMessage(message);
            }
        }, new Func1<String, Observable<Message>>() {
            @Override
            public Observable<Message> call(String url) {
                final PostMessage message = new PostMessage(null, ImmutableList.of(new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_MEDIA, null, null, ImmutableList.of(url), null)));
                return sendMessage(message);
            }
        }, conversationId));
    }

    public void sendLocation(double latitude, double longitude) {
        mSubscribe.add(sendMessage(mChatsDelegate.getLocationMessage(latitude, longitude))
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        mChatsDelegate.postLocalMessage(message, conversationId);
                        mListener.hideAttatchentsMenu();
                    }
                }, getOnError()));
    }

    public void deleteConversation() {
        if (conversationCreated) {
            mListener.conversationDeleted();
        } else {
            mSubscribe.add(mChatsDelegate.deleteConversation(conversationId));
        }
    }

    public void sendShout(final String shoutId) {
        mSubscribe.add(mApiService.getShout(shoutId)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .flatMap(new Func1<ShoutResponse, Observable<Message>>() {
                    @Override
                    public Observable<Message> call(ShoutResponse shoutResponse) {
                        return sendMessage(mChatsDelegate.getShoutMessage(shoutResponse, shoutId));
                    }
                })
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        mChatsDelegate.postLocalMessage(message, conversationId);
                        mListener.hideAttatchentsMenu();
                    }
                }, getOnError()));
    }

    public void sendTyping() {
        mChatsDelegate.sendTyping(conversationId);
    }

    public Observable<String> calledPersonUsernameObservable() {
        return calledPersonUsernameObservable;
    }
}
