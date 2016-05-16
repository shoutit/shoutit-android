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
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.utils.pusher.TypingInfo;
import com.shoutit.app.android.view.chats.ChatsDelegate;
import com.shoutit.app.android.view.chats.message_models.TypingItem;
import com.shoutit.app.android.view.conversations.ConversationsUtils;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func0;
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

        mChatsDelegate = new ChatsDelegate(pusher, uiScheduler, networkScheduler, apiService, resources, userPreferences, context, amazonHelper, newMessagesSubject);
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

    private void setupUserForVideoChat(@Nonnull User user) {
            chatParticipantUsernameSubject.onNext(user.getUsername());
            mUserPreferences.setShoutOwnerName(user.getName());
            mListener.showVideoChatIcon();
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
                        //noinspection ConstantConditions
                        mListener.setChatToolbarInfo(ConversationsUtils.getChatWithString(
                                ImmutableList.of(new ConversationProfile(
                                        user.getId(),
                                        user.getName(),
                                        user.getUsername(),
                                        user.getType(),
                                        user.getImage())), mUserPreferences.getUser().getId()));
                        setupUserForVideoChat(user);
                    }
                }, getOnError()));
    }

    private void getShout(final User user) {
        mSubscribe.add(mShoutsDao.getShoutObservable(mIdForCreation)
                .observeOn(mUiScheduler)
                .compose(ResponseOrError.<Shout>onlySuccess())
                .subscribe(new Action1<Shout>() {
                    @Override
                    public void call(Shout shout) {
                        final String title = shout.getTitle();
                        final String thumbnail = Strings.emptyToNull(shout.getThumbnail());
                        final String type = shout.getType().equals(Shout.TYPE_OFFER) ? mContext.getString(R.string.chat_offer) : mContext.getString(R.string.chat_request);
                        final String price = PriceUtils.formatPriceWithCurrency(shout.getPrice(), mResources, shout.getCurrency());
                        final User shoutOwner = shout.getProfile();
                        final String authorAndTime = shoutOwner.getName() + " - " + DateUtils.getRelativeTimeSpanString(mContext, shout.getDatePublishedInMillis());
                        final String id = shout.getId();

                        if (!Strings.isNullOrEmpty(id)) {
                            mListener.setAboutShoutData(title, thumbnail, type, price, authorAndTime, id);
                            mListener.setShoutToolbarInfo(title, ConversationsUtils.getChatWithString(
                                    ImmutableList.of(new ConversationProfile(
                                            shoutOwner.getId(),
                                            shoutOwner.getName(),
                                            shoutOwner.getUsername(),
                                            shoutOwner.getType(),
                                            shoutOwner.getImage())), user.getId()));
                        } else {
                            mListener.setShoutToolbarInfo(mContext.getString(R.string.chat_shout_chat), ConversationsUtils.getChatWithString(
                                    ImmutableList.of(new ConversationProfile(
                                            shoutOwner.getId(),
                                            shoutOwner.getName(),
                                            shoutOwner.getUsername(),
                                            shoutOwner.getType(),
                                            shoutOwner.getImage())), user.getId()));
                        }

                        setupUserForVideoChat(shoutOwner);
                    }
                }, getOnError()));
    }

    private void subscribeToMessages() {
        mLocalAndPusherMessagesSubject = PublishSubject.create();

        final Observable<PresenceChannel> channelObservable = Observable
                .defer(new Func0<Observable<PresenceChannel>>() {
                    @Override
                    public Observable<PresenceChannel> call() {

                        return Observable.just(mChatsDelegate.getConversationChannel(conversationId));
                    }
                })
                .cache();

        final Observable<List<PusherMessage>> localAndPusherMessages = mLocalAndPusherMessagesSubject.switchMap(
                new Func1<Object, Observable<PusherMessage>>() {
                    @Override
                    public Observable<PusherMessage> call(Object o) {
                        return channelObservable.flatMap(new Func1<PresenceChannel, Observable<PusherMessage>>() {
                            @Override
                            public Observable<PusherMessage> call(PresenceChannel presenceChannel) {
                                return mChatsDelegate.getPusherMessageObservable(presenceChannel).mergeWith(newMessagesSubject);
                            }
                        });
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
                }), mRefreshTypingObservable.switchMap(new Func1<Object, Observable<TypingInfo>>() {
                    @Override
                    public Observable<TypingInfo> call(Object o) {
                        return channelObservable.flatMap(new Func1<PresenceChannel, Observable<TypingInfo>>() {
                            @Override
                            public Observable<TypingInfo> call(PresenceChannel presenceChannel) {
                                return mChatsDelegate.getTypingObservable(presenceChannel);
                            }
                        });
                    }
                }), new Func2<List<BaseAdapterItem>, TypingInfo, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<BaseAdapterItem> baseAdapterItems, TypingInfo isTyping) {
                        if (isTyping.isTyping()) {
                            return ImmutableList.<BaseAdapterItem>builder()
                                    .addAll(baseAdapterItems)
                                    .add(new TypingItem(isTyping.getUsername()))
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
        if (conversationCreated) {
            return mApiService.postMessage(conversationId, message)
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler);
        } else {
            Observable<Message> observable;
            if (mIsShoutConversation) {
                observable = mApiService.createShoutConversation(mIdForCreation, message)
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler);
            } else {
                observable = mApiService.createChatConversation(mIdForCreation, message)
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler);
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
                final PostMessage message = new PostMessage(null, ImmutableList.of(
                        new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_MEDIA, null, null, null, ImmutableList.of(video), null)));
                return sendMessage(message);
            }
        }, new Func1<String, Observable<Message>>() {
            @Override
            public Observable<Message> call(String url) {
                final PostMessage message = new PostMessage(null, ImmutableList.of(
                        new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_MEDIA, null, null, ImmutableList.of(url), null, null)));
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
        mSubscribe.add(
                sendMessage(mChatsDelegate.getShoutMessage(shoutId))
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler)
                        .subscribe(new Action1<Message>() {
                            @Override
                            public void call(Message message) {
                                mChatsDelegate.postLocalMessage(message, conversationId);
                                mListener.hideAttatchentsMenu();
                            }
                        }, getOnError()));
    }

    public void sendProfile(@Nonnull String profileId) {
        mSubscribe.add(
                mApiService.postMessage(conversationId, mChatsDelegate.getProfileMessage(profileId))
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler)
                        .subscribe(new Action1<Message>() {
                            @Override
                            public void call(Message message) {
                                mChatsDelegate.postLocalMessage(message, conversationId);
                                mListener.hideAttatchentsMenu();
                            }
                        }, getOnError())
        );
    }

    public void sendTyping() {
        mChatsDelegate.sendTyping(conversationId);
    }

    public Observable<String> calledPersonUsernameObservable() {
        return calledPersonUsernameObservable;
    }
}
