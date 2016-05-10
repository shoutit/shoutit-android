package com.shoutit.app.android.view.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.pusher.client.channel.PresenceChannel;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ConversationProfile;
import com.shoutit.app.android.api.model.Message;
import com.shoutit.app.android.api.model.MessageAttachment;
import com.shoutit.app.android.api.model.PostMessage;
import com.shoutit.app.android.api.model.PusherMessage;
import com.shoutit.app.android.api.model.ShoutResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.utils.pusher.TypingInfo;
import com.shoutit.app.android.view.chats.message_models.DateItem;
import com.shoutit.app.android.view.chats.message_models.InfoItem;
import com.shoutit.app.android.view.chats.message_models.ReceivedImageMessage;
import com.shoutit.app.android.view.chats.message_models.ReceivedLocationMessage;
import com.shoutit.app.android.view.chats.message_models.ReceivedShoutMessage;
import com.shoutit.app.android.view.chats.message_models.ReceivedTextMessage;
import com.shoutit.app.android.view.chats.message_models.ReceivedVideoMessage;
import com.shoutit.app.android.view.chats.message_models.SentImageMessage;
import com.shoutit.app.android.view.chats.message_models.SentLocationMessage;
import com.shoutit.app.android.view.chats.message_models.SentShoutMessage;
import com.shoutit.app.android.view.chats.message_models.SentTextMessage;
import com.shoutit.app.android.view.chats.message_models.SentVideoMessage;
import com.shoutit.app.android.view.media.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

public class ChatsDelegate {

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("hh:mm");

    private final PusherHelper mPusher;
    private final Scheduler mUiScheduler;
    private final Scheduler mNetworkScheduler;
    private final ApiService mApiService;
    private final Resources mResources;
    private final UserPreferences mUserPreferences;
    private final Context mContext;
    private final AmazonHelper mAmazonHelper;
    private Listener mListener;
    private final User mUser;
    private final PublishSubject<PusherMessage> newMessagesSubject;


    public ChatsDelegate(PusherHelper pusher, Scheduler uiScheduler, Scheduler networkScheduler, ApiService apiService, Resources resources, UserPreferences userPreferences, Context context, AmazonHelper amazonHelper, PublishSubject<PusherMessage> newMessagesSubject) {
        mPusher = pusher;
        mUiScheduler = uiScheduler;
        mNetworkScheduler = networkScheduler;
        mApiService = apiService;
        mUserPreferences = userPreferences;
        mResources = resources;
        mContext = context;
        mAmazonHelper = amazonHelper;
        this.newMessagesSubject = newMessagesSubject;
        mUser = mUserPreferences.getUser();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = null;
    }

    public PresenceChannel getConversationChannel(String conversationId) {
        return mPusher.subscribeConversationChannel(conversationId);
    }

    public Observable<PusherMessage> getPusherMessageObservable(PresenceChannel presenceChannel) {
        return mPusher.getNewMessageObservable(presenceChannel)
                .flatMap(new Func1<PusherMessage, Observable<PusherMessage>>() {
                    @Override
                    public Observable<PusherMessage> call(final PusherMessage pusherMessage) {
                        final String id = pusherMessage.getId();
                        if (mUser.getId().equals(id)) {
                            return Observable.just(pusherMessage);
                        } else {
                            return mApiService.readMessage(id)
                                    .map(new Func1<ResponseBody, PusherMessage>() {
                                        @Override
                                        public PusherMessage call(ResponseBody responseBody) {
                                            return pusherMessage;
                                        }
                                    });
                        }
                    }
                })
                .observeOn(mUiScheduler);
    }

    public Observable<TypingInfo> getTypingObservable(PresenceChannel presenceChannel) {
        return mPusher.getIsTypingObservable(presenceChannel)
                .switchMap(new Func1<TypingInfo, Observable<TypingInfo>>() {
                    @Override
                    public Observable<TypingInfo> call(TypingInfo typingInfo) {
                        return Observable.timer(3, TimeUnit.SECONDS)
                                .map(new Func1<Long, TypingInfo>() {
                                    @Override
                                    public TypingInfo call(Long aLong) {
                                        return TypingInfo.notTyping();
                                    }
                                })
                                .startWith(typingInfo);
                    }
                })
                .observeOn(mUiScheduler)
                .startWith(TypingInfo.notTyping());
    }

    public Observable.Transformer<PusherMessage, List<PusherMessage>> transformToScan() {
        return new Observable.Transformer<PusherMessage, List<PusherMessage>>() {
            @Override
            public Observable<List<PusherMessage>> call(Observable<PusherMessage> observable) {
                return observable.scan(ImmutableList.<PusherMessage>of(), new Func2<List<PusherMessage>, PusherMessage, List<PusherMessage>>() {
                    @Override
                    public List<PusherMessage> call(List<PusherMessage> pusherMessages, PusherMessage pusherMessage) {
                        if (containsMessage(pusherMessages, pusherMessage)) {
                            return pusherMessages;
                        } else {
                            return ImmutableList.<PusherMessage>builder()
                                    .addAll(pusherMessages)
                                    .add(pusherMessage)
                                    .build();
                        }
                    }
                });
            }
        };
    }

    private boolean containsMessage(@NonNull List<PusherMessage> pusherMessages, @NonNull PusherMessage pusherMessage) {
        for (PusherMessage listPusherMessage : pusherMessages) {
            if (listPusherMessage.getId().equals(pusherMessage.getId())) return true;
        }
        return false;
    }

    public void messagesSuccess(@NonNull List<BaseAdapterItem> baseAdapterItems, Listener listener) {
        listener.showProgress(false);
        if (baseAdapterItems.isEmpty()) {
            listener.emptyList();
        } else {
            listener.setData(baseAdapterItems);
        }
    }

    public void messagesError(Throwable throwable, Listener listener) {
        listener.showProgress(false);
        listener.error(throwable);
    }

    @NonNull
    public List<BaseAdapterItem> transform(@NonNull List<Message> results) {
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

    public void sendTyping(String conversationId) {
        mPusher.sendTyping(conversationId, mUser.getId(), mUser.getUsername());
    }

    public PostMessage getShoutMessage(ShoutResponse shoutResponse, String shoutId) {
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
        return new PostMessage(null, ImmutableList.of(new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_LOCATION, null, new MessageAttachment.AttachtmentShout(shoutId, null, null, shoutResponse.getType(), shoutResponse.getLocation(), shoutResponse.getTitle(), shoutResponse.getText(), shoutResponse.getPrice(), 0, shoutResponse.getCurrency(), thumbnail, videoUrl, shoutResponse.getProfile(), shoutResponse.getCategory(), shoutResponse.getDatePublished(), 0), null, null)));
    }

    public Subscription deleteConversation(String conversationId) {
        return mApiService.deleteConversation(conversationId)
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        mListener.conversationDeleted();
                    }
                }, getOnError());
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

    public PostMessage getLocationMessage(double latitude, double longitude) {
        return new PostMessage(null, ImmutableList.of(new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_LOCATION, new MessageAttachment.MessageLocation(latitude, longitude), null, null, null)));
    }

    public Subscription addMedia(@NonNull String media, boolean isVideo, Func1<Video, Observable<Message>> videoToMessageFunc, Func1<String, Observable<Message>> photoToMessageFunc, final String conversationId) {
        mListener.showProgress(true);
        if (isVideo) {
            try {
                final File videoThumbnail = MediaUtils.createVideoThumbnail(mContext, Uri.parse(media));
                final int videoLength = MediaUtils.getVideoLength(mContext, media);
                final Observable<String> videoFileObservable = mAmazonHelper.uploadShoutMediaVideoObservable(AmazonHelper.getfileFromPath(media));
                final Observable<String> thumbFileObservable = mAmazonHelper.uploadShoutMediaImageObservable(AmazonHelper.getfileFromPath(videoThumbnail.getAbsolutePath()));
                return Observable
                        .zip(videoFileObservable, thumbFileObservable, new Func2<String, String, Video>() {
                            @Override
                            public Video call(String video, String thumb) {
                                return Video.createVideo(video, thumb, videoLength);
                            }
                        })
                        .flatMap(videoToMessageFunc)
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler)
                        .subscribe(new Action1<Message>() {
                            @Override
                            public void call(Message messagesResponse) {
                                postLocalMessage(messagesResponse, conversationId);
                                mListener.showProgress(false);
                                mListener.hideAttatchentsMenu();
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                mListener.showProgress(false);
                                mListener.error(throwable);
                            }
                        });
            } catch (IOException e) {
                mListener.error(e);
                return Subscriptions.empty();
            }
        } else {
            return mAmazonHelper.uploadShoutMediaImageObservable(AmazonHelper.getfileFromPath(media))
                    .flatMap(photoToMessageFunc)
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler)
                    .subscribe(new Action1<Message>() {
                        @Override
                        public void call(Message messagesResponse) {
                            postLocalMessage(messagesResponse, conversationId);
                            mListener.showProgress(false);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mListener.showProgress(false);
                            mListener.error(throwable);
                        }
                    });
        }
    }

    public void postLocalMessage(Message messagesResponse, String conversationId) {
        newMessagesSubject.onNext(new PusherMessage(
                messagesResponse.getProfile(),
                conversationId,
                messagesResponse.getId(),
                messagesResponse.getText(),
                messagesResponse.getAttachments(),
                messagesResponse.getCreatedAt()));
    }
}
