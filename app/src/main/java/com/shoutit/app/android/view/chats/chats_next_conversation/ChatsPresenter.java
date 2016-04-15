package com.shoutit.app.android.view.chats.chats_next_conversation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.pusher.client.channel.PresenceChannel;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.AboutShout;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.ConversationProfile;
import com.shoutit.app.android.api.model.Message;
import com.shoutit.app.android.api.model.MessageAttachment;
import com.shoutit.app.android.api.model.MessagesResponse;
import com.shoutit.app.android.api.model.PostMessage;
import com.shoutit.app.android.api.model.PusherMessage;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.PusherHelper;
import com.shoutit.app.android.view.chats.Listener;
import com.shoutit.app.android.view.chats.PresenceChannelEventListenerAdapter;
import com.shoutit.app.android.view.chats.models.DateItem;
import com.shoutit.app.android.view.chats.models.InfoItem;
import com.shoutit.app.android.view.chats.models.ReceivedImageMessage;
import com.shoutit.app.android.view.chats.models.ReceivedLocationMessage;
import com.shoutit.app.android.view.chats.models.ReceivedShoutMessage;
import com.shoutit.app.android.view.chats.models.ReceivedTextMessage;
import com.shoutit.app.android.view.chats.models.ReceivedVideoMessage;
import com.shoutit.app.android.view.chats.models.SentImageMessage;
import com.shoutit.app.android.view.chats.models.SentLocationMessage;
import com.shoutit.app.android.view.chats.models.SentShoutMessage;
import com.shoutit.app.android.view.chats.models.SentTextMessage;
import com.shoutit.app.android.view.chats.models.SentVideoMessage;
import com.shoutit.app.android.view.chats.models.TypingItem;
import com.shoutit.app.android.view.conversations.ConversationsUtils;
import com.shoutit.app.android.view.media.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class ChatsPresenter {

    final OperatorMergeNextToken<MessagesResponse, Object> loadMoreOperator =
            OperatorMergeNextToken.create(new Func1<MessagesResponse, Observable<MessagesResponse>>() {

                @Override
                public Observable<MessagesResponse> call(MessagesResponse conversationsResponse) {
                    if (conversationsResponse == null || conversationsResponse.getNext() != null) {
                        if (conversationsResponse == null) {
                            return mApiService.getMessages(conversationId)
                                    .subscribeOn(mNetworkScheduler)
                                    .observeOn(mUiScheduler);
                        } else {
                            final String after = Uri.parse(conversationsResponse.getNext()).getQueryParameter("after");
                            return Observable.just(
                                    conversationsResponse)
                                    .zipWith(
                                            mApiService.getMessages(conversationId, after)
                                                    .subscribeOn(mNetworkScheduler)
                                                    .observeOn(mUiScheduler),
                                            new Func2<MessagesResponse, MessagesResponse, MessagesResponse>() {
                                                @Override
                                                public MessagesResponse call(MessagesResponse conversationsResponse, MessagesResponse newResponse) {
                                                    return new MessagesResponse(newResponse.getNext(),
                                                            ImmutableList.copyOf(Iterables.concat(
                                                                    conversationsResponse.getResults(),
                                                                    newResponse.getResults())));
                                                }
                                            });
                        }
                    } else {
                        return Observable.never();
                    }
                }
            });

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("hh:mm");

    @NonNull
    private final String conversationId;
    @NonNull
    private final ApiService mApiService;
    private final Scheduler mUiScheduler;
    private final Scheduler mNetworkScheduler;
    private final UserPreferences mUserPreferences;
    private final Resources mResources;
    private final Context mContext;
    private final PusherHelper mPusher;
    private final Gson mGson;
    private final AmazonHelper mAmazonHelper;
    private final boolean mIsShoutConversation;
    private Listener mListener;
    private CompositeSubscription mSubscribe = new CompositeSubscription();
    private final PublishSubject<Object> requestSubject = PublishSubject.create();
    private final PublishSubject<PusherMessage> newMessagesSubject = PublishSubject.create();
    private final User mUser;

    @Inject
    public ChatsPresenter(@NonNull String conversationId,
                          @NonNull ApiService apiService,
                          @UiScheduler Scheduler uiScheduler,
                          @NetworkScheduler Scheduler networkScheduler,
                          UserPreferences userPreferences,
                          @ForActivity Resources resources,
                          @ForActivity Context context,
                          PusherHelper pusher,
                          Gson gson,
                          AmazonHelper amazonHelper,
                          boolean isShoutConversation) {
        this.conversationId = conversationId;
        mApiService = apiService;
        mUiScheduler = uiScheduler;
        mNetworkScheduler = networkScheduler;
        mUserPreferences = userPreferences;
        mResources = resources;
        mContext = context;
        mPusher = pusher;
        mGson = gson;
        mAmazonHelper = amazonHelper;
        mIsShoutConversation = isShoutConversation;
        mUser = mUserPreferences.getUser();
    }

    public void register(@NonNull Listener listener) {
        final User user = mUserPreferences.getUser();
        assert user != null;
        final PresenceChannel userChannel = mPusher.getPusher().getPresenceChannel(String.format("presence-v3-p-%1$s", user.getId()));
        final PresenceChannel conversationChannel = mPusher.getPusher().subscribePresence(String.format("presence-v3-c-%1$s", conversationId));

        final Observable<PusherMessage> pusherMessageObservable = Observable
                .create(new Observable.OnSubscribe<PusherMessage>() {
                    @Override
                    public void call(final Subscriber<? super PusherMessage> subscriber) {
                        userChannel.bind("new_message", new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                try {
                                    final PusherMessage pusherMessage = mGson.getAdapter(PusherMessage.class).fromJson(data);
                                    if (pusherMessage.getConversationId().equals(conversationId)) {
                                        subscriber.onNext(pusherMessage);
                                    }
                                } catch (IOException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .observeOn(mUiScheduler);

        final Observable<Boolean> isTyping = Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(final Subscriber<? super Boolean> subscriber) {
                        conversationChannel.bind("client-is_typing", new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                subscriber.onNext(true);
                            }
                        });
                    }
                })
                .switchMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean aBoolean) {
                        return Observable.timer(3, TimeUnit.SECONDS).map(Functions1.returnFalse())
                                .startWith(true);
                    }
                })
                .observeOn(mUiScheduler)
                .startWith(false);

        final Observable<MessagesResponse> apiMessages = requestSubject
                .startWith(new Object())
                .lift(loadMoreOperator)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler);

        final Observable<List<PusherMessage>> localAndPusherMessages = pusherMessageObservable.mergeWith(newMessagesSubject)
                .scan(ImmutableList.<PusherMessage>of(), new Func2<List<PusherMessage>, PusherMessage, List<PusherMessage>>() {
                    @Override
                    public List<PusherMessage> call(List<PusherMessage> pusherMessages, PusherMessage pusherMessage) {
                        return ImmutableList.<PusherMessage>builder()
                                .addAll(pusherMessages)
                                .add(pusherMessage)
                                .build();
                    }
                });

        mListener = listener;
        mListener.showProgress(true);
        mSubscribe.add(Observable.combineLatest(apiMessages, localAndPusherMessages.startWith((List<PusherMessage>) null), isTyping,
                new Func3<MessagesResponse, List<PusherMessage>, Boolean, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(MessagesResponse messagesResponse, List<PusherMessage> pusherMessage, Boolean isTyping) {
                        final ImmutableList.Builder<Message> builder = ImmutableList.<Message>builder()
                                .addAll(messagesResponse.getResults());

                        if (pusherMessage != null) {
                            for (PusherMessage message : pusherMessage) {
                                builder.add(new Message(
                                        conversationId, message.getProfile(),
                                        message.getId(),
                                        message.getText(),
                                        message.getAttachments(),
                                        message.getCreatedAt()));
                            }
                        }

                        final List<BaseAdapterItem> baseAdapterItemList = transform(builder.build());

                        if (isTyping) {
                            return ImmutableList.<BaseAdapterItem>builder()
                                    .addAll(baseAdapterItemList)
                                    .add(new TypingItem())
                                    .build();
                        } else {
                            return baseAdapterItemList;
                        }
                    }
                })
                .subscribe(new Action1<List<BaseAdapterItem>>() {
                    @Override
                    public void call(@NonNull List<BaseAdapterItem> baseAdapterItems) {
                        mListener.showProgress(false);
                        if (baseAdapterItems.isEmpty()) {
                            mListener.emptyList();
                        } else {
                            mListener.setData(baseAdapterItems);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mListener.showProgress(false);
                        mListener.error(throwable);
                    }
                }));


        mSubscribe.add(mApiService.getConversation(conversationId)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<Conversation>() {
                    @Override
                    public void call(Conversation conversationResponse) {
                        if (mIsShoutConversation) {
                            final AboutShout about = conversationResponse.getAbout();
                            final String title = about.getTitle();
                            final String thumbnail = Strings.emptyToNull(about.getThumbnail());
                            final String type = about.getType().equals(Shout.TYPE_OFFER) ? mContext.getString(R.string.chat_offer) : mContext.getString(R.string.chat_request);
                            final String price = PriceUtils.formatPriceWithCurrency(about.getPrice(), mResources, about.getCurrency());
                            final String authorAndTime = about.getProfile().getName() + " - " + DateUtils.getRelativeTimeSpanString(mContext, about.getDatePublished() * 1000);
                            final String id = about.getId();

                            if (!Strings.isNullOrEmpty(id)) {
                                mListener.setAboutShoutData(title, thumbnail, type, price, authorAndTime, id);
                                mListener.setShoutToolbarInfo(title, ConversationsUtils.getChatWithString(conversationResponse.getProfiles()));
                            } else {
                                mListener.setShoutToolbarInfo(mContext.getString(R.string.chat_shout_chat), ConversationsUtils.getChatWithString(conversationResponse.getProfiles()));
                            }
                        } else {
                            mListener.setChatToolbatInfo(ConversationsUtils.getChatWithString(conversationResponse.getProfiles()));
                        }
                    }
                }, getOnError()));
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

    @NonNull
    public Observer<Object> getRequestSubject() {
        return requestSubject;
    }

    @NonNull
    private List<BaseAdapterItem> transform(@NonNull List<Message> results) {
        final User user = mUserPreferences.getUser();
        assert user != null;
        final String userId = user.getId();

        final List<BaseAdapterItem> objects = Lists.newArrayList();
        for (int i = 0; i < results.size(); i++) {

            final DateItem dateItem = getDateItem(results, i);
            if (dateItem != null) {
                objects.add(dateItem);
            }

            final BaseAdapterItem item = getItem(results, userId, i);
            if (item != null) {
                objects.add(item);
            }
        }

        return ImmutableList.copyOf(objects);
    }

    @Nullable
    private DateItem getDateItem(@NonNull List<Message> results, int currentPosition) {
        final Message currentMessage = results.get(currentPosition);
        if (currentPosition == 0) {
            final String date = mSimpleDateFormat.format(new Date(currentMessage.getCreatedAt() * 1000));
            return new DateItem(date);
        } else {
            final long currentCreatedAt = currentMessage.getCreatedAt();
            final long previousCreatedAt = results.get(currentPosition - 1).getCreatedAt();

            final Calendar currentCalendar = Calendar.getInstance();
            final Calendar previousCalendar = Calendar.getInstance();

            currentCalendar.setTimeInMillis(currentCreatedAt * 1000);
            previousCalendar.setTimeInMillis(previousCreatedAt * 1000);

            final int currentDayOfTheYear = currentCalendar.get(Calendar.DAY_OF_YEAR);
            final int currentYear = currentCalendar.get(Calendar.YEAR);

            final int previousDayOfTheYear = previousCalendar.get(Calendar.DAY_OF_YEAR);
            final int previousYear = previousCalendar.get(Calendar.YEAR);

            if (currentYear != previousYear || currentDayOfTheYear != previousDayOfTheYear) {
                final String date = mSimpleDateFormat.format(new Date(currentMessage.getCreatedAt() * 1000));
                return new DateItem(date);
            } else {
                return null;
            }
        }
    }

    public void postTextMessage(@NonNull String text) {
        mApiService.postMessage(conversationId, new PostMessage(text, ImmutableList.<MessageAttachment>of()))
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message messagesResponse) {
                        postLocalMessage(messagesResponse);
                    }
                }, getOnError());
    }

    private BaseAdapterItem getItem(@NonNull List<Message> results, String userId, int currentPosition) {
        final Message message = results.get(currentPosition);

        final ConversationProfile profile = message.getProfile();
        if (profile != null) {
            final String messageProfileId = profile.getId();

            final String time = mSimpleTimeFormat.format(new Date(message.getCreatedAt() * 1000));
            if (messageProfileId.equals(userId)) {
                return getSentItem(message, time);
            } else {
                final boolean isFirst = isFirst(currentPosition, results, messageProfileId);
                return getReceivedItem(message, isFirst, time);
            }
        } else {
            return new InfoItem(results.get(currentPosition).getText());
        }
    }

    private boolean isFirst(int position, @NonNull List<Message> results, @NonNull String currentMessageUserId) {
        if (position == 0) {
            return true;
        } else {
            final Message prevMessage = results.get(position - 1);
            return !prevMessage.getProfile().getId().equals(currentMessageUserId);
        }
    }

    private BaseAdapterItem getReceivedItem(Message message, boolean isFirst, String time) {
        final List<MessageAttachment> attachments = message.getAttachments();
        final String avatarUrl = message.getProfile().getImage();
        if (attachments.isEmpty()) {
            return new ReceivedTextMessage(isFirst, time, message.getText(), avatarUrl);
        } else {
            final MessageAttachment messageAttachment = attachments.get(0);
            final String type = messageAttachment.getType();
            if (MessageAttachment.ATTACHMENT_TYPE_MEDIA.equals(type)) {
                final List<String> images = messageAttachment.getImages();
                if (images != null && !images.isEmpty()) {
                    return new ReceivedImageMessage(isFirst, time, images.get(0), avatarUrl, mListener);
                } else {
                    final Video video = messageAttachment.getVideos().get(0);
                    return new ReceivedVideoMessage(isFirst, video.getThumbnailUrl(), time, avatarUrl, mListener, video.getUrl());
                }
            } else if (MessageAttachment.ATTACHMENT_TYPE_LOCATION.equals(type)) {
                final MessageAttachment.MessageLocation location = messageAttachment.getLocation();
                return new ReceivedLocationMessage(isFirst, time, avatarUrl, mListener, location.getLatitude(), location.getLongitude());
            } else if (MessageAttachment.ATTACHMENT_TYPE_SHOUT.equals(type)) {
                final MessageAttachment.AttachtmentShout shout = messageAttachment.getShout();
                return new ReceivedShoutMessage(
                        isFirst,
                        shout.getThumbnailOrNull(),
                        time,
                        PriceUtils.formatPriceWithCurrency(shout.getPrice(), mResources, shout.getCurrency()),
                        shout.getText(),
                        shout.getUser().getName(),
                        avatarUrl, mListener, shout.getId());
            } else {
                throw new RuntimeException(type);
            }
        }
    }

    private BaseAdapterItem getSentItem(Message message, String time) {
        final List<MessageAttachment> attachments = message.getAttachments();
        if (attachments.isEmpty()) {
            return new SentTextMessage(time, message.getText());
        } else {
            final MessageAttachment messageAttachment = attachments.get(0);
            final String type = messageAttachment.getType();
            if (MessageAttachment.ATTACHMENT_TYPE_MEDIA.equals(type)) {
                final List<String> images = messageAttachment.getImages();
                if (images != null && !images.isEmpty()) {
                    return new SentImageMessage(time, images.get(0), mListener);
                } else {
                    final Video video = messageAttachment.getVideos().get(0);
                    return new SentVideoMessage(video.getThumbnailUrl(), time, mListener, video.getUrl());
                }
            } else if (MessageAttachment.ATTACHMENT_TYPE_LOCATION.equals(type)) {
                final MessageAttachment.MessageLocation location = messageAttachment.getLocation();
                return new SentLocationMessage(time, mListener, location.getLatitude(), location.getLongitude());
            } else if (MessageAttachment.ATTACHMENT_TYPE_SHOUT.equals(type)) {
                final MessageAttachment.AttachtmentShout shout = messageAttachment.getShout();
                return new SentShoutMessage(shout.getThumbnailOrNull(), time, PriceUtils.formatPriceWithCurrency(shout.getPrice(), mResources, shout.getCurrency()), shout.getText(), shout.getUser().getName(), mListener, shout.getId());
            } else {
                throw new RuntimeException(type);
            }
        }
    }

    public void unregister() {
        mListener = null;
        mSubscribe.unsubscribe();
        mPusher.getPusher().unsubscribe(String.format("presence-v3-c-%1$s", conversationId));
    }

    public void addMedia(@NonNull String media, boolean isVideo) {
        mListener.showProgress(true);
        if (isVideo) {
            try {
                final File videoThumbnail = MediaUtils.createVideoThumbnail(mContext, Uri.parse(media));
                final int videoLength = MediaUtils.getVideoLength(mContext, media);
                final Observable<String> videoFileObservable = mAmazonHelper.uploadShoutMediaVideoObservable(AmazonHelper.getfileFromPath(media));
                final Observable<String> thumbFileObservable = mAmazonHelper.uploadShoutMediaImageObservable(AmazonHelper.getfileFromPath(videoThumbnail.getAbsolutePath()));
                mSubscribe.add(Observable
                        .zip(videoFileObservable, thumbFileObservable, new Func2<String, String, Video>() {
                            @Override
                            public Video call(String video, String thumb) {
                                return Video.createVideo(video, thumb, videoLength);
                            }
                        })
                        .flatMap(new Func1<Video, Observable<Message>>() {
                            @Override
                            public Observable<Message> call(Video video) {
                                return mApiService.postMessage(conversationId, new PostMessage(null, ImmutableList.of(new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_MEDIA, null, null, null, ImmutableList.of(video)))))
                                        .subscribeOn(mNetworkScheduler)
                                        .observeOn(mUiScheduler);
                            }
                        })
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler)
                        .subscribe(new Action1<Message>() {
                            @Override
                            public void call(Message messagesResponse) {
                                mListener.hideAttatchentsMenu();
                                postLocalMessage(messagesResponse);
                                mListener.showProgress(false);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                mListener.showProgress(false);
                                mListener.error(throwable);
                            }
                        }));
            } catch (IOException e) {
                mListener.error(e);
            }
        } else {
            mSubscribe.add(mAmazonHelper.uploadShoutMediaImageObservable(AmazonHelper.getfileFromPath(media))
                    .flatMap(new Func1<String, Observable<Message>>() {
                        @Override
                        public Observable<Message> call(String url) {
                            return mApiService.postMessage(
                                    conversationId,
                                    new PostMessage(null, ImmutableList.of(new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_MEDIA, null, null, ImmutableList.of(url), null))))
                                    .subscribeOn(mNetworkScheduler)
                                    .observeOn(mUiScheduler);

                        }
                    })
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler)
                    .subscribe(new Action1<Message>() {
                        @Override
                        public void call(Message messagesResponse) {
                            postLocalMessage(messagesResponse);
                            mListener.showProgress(false);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mListener.showProgress(false);
                            mListener.error(throwable);
                        }
                    }));
        }
    }

    private void postLocalMessage(Message messagesResponse) {
        newMessagesSubject.onNext(new PusherMessage(
                messagesResponse.getProfile(),
                conversationId,
                messagesResponse.getId(),
                messagesResponse.getText(),
                messagesResponse.getAttachments(),
                messagesResponse.getCreatedAt()));
    }

    public void sendLocation(double latitude, double longitude) {
        mApiService.postMessage(conversationId, new PostMessage(null, ImmutableList.of(new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_LOCATION, new MessageAttachment.MessageLocation(latitude, longitude), null, null, null))))
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        postLocalMessage(message);
                        mListener.hideAttatchentsMenu();
                    }
                }, getOnError());
    }


    public void deleteShout() {
        mApiService.deleteConversation(conversationId)
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        mListener.conversationDeleted();
                    }
                }, getOnError());
    }

    public void sendShout(final String shoutId) {
        mApiService.getShout(shoutId)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .flatMap(new Func1<ShoutResponse, Observable<Message>>() {
                    @Override
                    public Observable<Message> call(ShoutResponse shoutResponse) {
                        final List<String> images = shoutResponse.getImages();
                        final List<Video> videos = shoutResponse.getVideos();
                        String thumbnail = null;
                        String videoUrl = null;
                        if (videos != null && !videos.isEmpty()) {
                            final Video video = videos.get(0);
                            thumbnail = video.getThumbnailUrl();
                            videoUrl = video.getUrl();
                        } else if (images != null && !images.isEmpty()) {
                            thumbnail = images.get(0);
                        }
                        return mApiService.postMessage(conversationId, new PostMessage(null, ImmutableList.of(new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_LOCATION, null, new MessageAttachment.AttachtmentShout(shoutId, null, null, shoutResponse.getType(), shoutResponse.getLocation(), shoutResponse.getTitle(), shoutResponse.getText(), shoutResponse.getPrice(), 0, shoutResponse.getCurrency(), thumbnail, videoUrl, shoutResponse.getProfile(), shoutResponse.getCategory(), shoutResponse.getDatePublished(), 0), null, null))))
                                .subscribeOn(mNetworkScheduler)
                                .observeOn(mUiScheduler);
                    }
                })
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        postLocalMessage(message);
                        mListener.hideAttatchentsMenu();
                    }
                }, getOnError());
    }

    public void sendTyping() {
        final PresenceChannel presenceChannel = mPusher.getPusher().getPresenceChannel(String.format("presence-v3-c-%1$s", conversationId));
        if (presenceChannel != null && presenceChannel.isSubscribed()) {
            presenceChannel.trigger("client-is_typing", mGson.toJson(mUser));
        }
    }
}
