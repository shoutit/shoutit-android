package com.shoutit.app.android.view.chats;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Message;
import com.shoutit.app.android.api.model.MessageAttachment;
import com.shoutit.app.android.api.model.MessagesResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.view.chats.message_models.DateItem;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

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
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MMMMM dd, yyyy");
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
    private Listener mListener;
    private Subscription mSubscribe;
    private final PublishSubject<Object> requestSubject = PublishSubject.create();

    @Inject
    public ChatsPresenter(@NonNull String conversationId,
                          @NonNull ApiService apiService,
                          @UiScheduler Scheduler uiScheduler,
                          @NetworkScheduler Scheduler networkScheduler,
                          UserPreferences userPreferences,
                          @ForActivity Resources resources) {
        this.conversationId = conversationId;
        mApiService = apiService;
        mUiScheduler = uiScheduler;
        mNetworkScheduler = networkScheduler;
        mUserPreferences = userPreferences;
        mResources = resources;
    }

    public void register(@NonNull Listener listener) {
        mListener = listener;
        mListener.showProgress(true);
        mSubscribe = requestSubject
                .startWith(new Object())
                .lift(loadMoreOperator)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(
                        new Action1<MessagesResponse>() {
                            @Override
                            public void call(@NonNull MessagesResponse messagesResponse) {
                                mListener.showProgress(false);
                                if (messagesResponse.getResults().isEmpty()) {
                                    mListener.emptyList();
                                } else {
                                    mListener.setData(transform(messagesResponse.getResults()));
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                mListener.showProgress(false);
                                mListener.error();
                            }
                        });
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

            objects.add(getItem(results, userId, i));
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

        
        final String messageProfileId = message.getProfile().getId();

        final String time = mSimpleTimeFormat.format(new Date(message.getCreatedAt() * 1000));
        if (messageProfileId.equals(userId)) {
            return getSentItem(message, time);
        } else {
            final boolean isFirst = isFirst(currentPosition, results, messageProfileId);
            return getReceivedItem(message, isFirst, time);
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
            if (MessageAttachment.ATTACHMENT_TYPE_IMAGE.equals(type)) {
                return new ReceivedImageMessage(isFirst, time, messageAttachment.getImage().getUrl(), avatarUrl);
            } else if (MessageAttachment.ATTACHMENT_TYPE_VIDEO.equals(type)) {
                return new ReceivedVideoMessage(isFirst, messageAttachment.getVideo().getThumbnailUrl(), time, avatarUrl);
            } else if (MessageAttachment.ATTACHMENT_TYPE_LOCATION.equals(type)) {
                return new ReceivedLocationMessage(isFirst, time, avatarUrl);
            } else if (MessageAttachment.ATTACHMENT_TYPE_SHOUT.equals(type)) {
                final Shout shout = messageAttachment.getShout();
                return new ReceivedShoutMessage(
                        isFirst,
                        shout.getThumbnail(),
                        time,
                        PriceUtils.formatPriceWithCurrency(shout.getPrice(), mResources, shout.getCurrency()),
                        shout.getText(),
                        shout.getProfile().getName(),
                        avatarUrl);
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
            if (MessageAttachment.ATTACHMENT_TYPE_IMAGE.equals(type)) {
                return new SentImageMessage(time, messageAttachment.getImage().getUrl());
            } else if (MessageAttachment.ATTACHMENT_TYPE_VIDEO.equals(type)) {
                return new SentVideoMessage(messageAttachment.getVideo().getThumbnailUrl(), time);
            } else if (MessageAttachment.ATTACHMENT_TYPE_LOCATION.equals(type)) {
                return new SentLocationMessage(time);
            } else if (MessageAttachment.ATTACHMENT_TYPE_SHOUT.equals(type)) {
                final Shout shout = messageAttachment.getShout();
                return new SentShoutMessage(shout.getThumbnail(), time, PriceUtils.formatPriceWithCurrency(shout.getPrice(), mResources, shout.getCurrency()), shout.getText(), shout.getProfile().getName());
            } else {
                throw new RuntimeException(type);
            }
        }
    }

    public void unregister() {
        mListener = null;
        mSubscribe.unsubscribe();
    }

    public interface Listener {

        void emptyList();

        void showProgress(boolean show);

        void setData(@NonNull List<BaseAdapterItem> items);

        void error();

    }
}
