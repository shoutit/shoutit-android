package com.shoutit.app.android.view.conversations;

import android.support.annotation.NonNull;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ConversationsResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;

public class ConversationsPresenter {

    @NonNull
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private Listener mListener;
    private Subscription mSubscription;

    @Inject
    public ConversationsPresenter(@NonNull ApiService apiService,
                                  @NetworkScheduler Scheduler networkScheduler,
                                  @UiScheduler Scheduler uiScheduler) {
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
    }

    public void register(@NonNull Listener listener) {
        mListener = listener;

        mListener.showProgress(true);

        mSubscription = mApiService.getConversations()
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<ConversationsResponse>() {
                    @Override
                    public void call(ConversationsResponse conversationsResponse) {
                        mListener.showProgress(false);

                        final List<ConversationsResponse.Conversation> conversations = conversationsResponse.getResults();
                        if (conversations.isEmpty()) {
                            mListener.emptyList();
                        } else {
                            Iterables.transform(conversations, new Function<ConversationsResponse.Conversation, ConversationItem>() {
                                @Nullable
                                @Override
                                public ConversationItem apply(@Nullable ConversationsResponse.Conversation input) {
                                    assert input != null;
                                    final List<ConversationsResponse.ConversationProfile> profiles = input.getProfiles();
                                    final ConversationsResponse.Message lastMessage = input.getLastMessage();
                                    final long createdAt = lastMessage.getCreatedAt();
                                    

                                    if (ConversationsResponse.Conversation.ABOUT_SHOUT_TYPE.equals(input.getType())) {
                                        return new ConversationShoutItem(input.getId(), input.getAbout().getTitle(), profiles, lastMessage, )
                                    } else if (ConversationsResponse.Conversation.CHAT_TYPE.equals(input.getType())) {
                                        return new ConversationChatItem(input.getId(), null, null, );
                                    }
                                }
                            });
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

    public void unregister() {
        mListener = null;
        mSubscription.unsubscribe();
    }

    public interface Listener {

        void emptyList();

        void showProgress(boolean show);

        void setData();

        void error();
    }

    public abstract class ConversationItem {

    }

    public class ConversationChatItem extends ConversationItem {
        private final String id;
        private final String message;
        private final String user;
        private final String time;

        public ConversationChatItem(String id, String message, String user, String time) {
            this.id = id;
            this.message = message;
            this.user = user;
            this.time = time;
        }
    }

    public class ConversationShoutItem extends ConversationItem {
        private final String id;
        private final String shoutDescription;
        private final String userNames;
        private final String message;
        private final String time;

        public ConversationShoutItem(String id, String shoutDescription, String userNames, String message, String time) {
            this.id = id;
            this.shoutDescription = shoutDescription;
            this.userNames = userNames;
            this.message = message;
            this.time = time;
        }
    }
}
