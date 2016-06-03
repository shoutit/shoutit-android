package com.shoutit.app.android.view.conversations;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.ConversationsResponse;
import com.shoutit.app.android.api.model.PusherMessage;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.pusher.PusherHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
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

public class ConversationsPresenter {

    private static final int PAGE_SIZE = 20;

    final OperatorMergeNextToken<ConversationsResponse, Object> loadMoreOperator =
            OperatorMergeNextToken.create(new Func1<ConversationsResponse, Observable<ConversationsResponse>>() {

                @Override
                public Observable<ConversationsResponse> call(ConversationsResponse conversationsResponse) {
                    if (conversationsResponse == null || conversationsResponse.getPrevious() != null) {
                        if (conversationsResponse == null) {
                            return getConversationsRequest(null)
                                    .subscribeOn(mNetworkScheduler)
                                    .observeOn(mUiScheduler);
                        } else {
                            final String before = Uri.parse(conversationsResponse.getPrevious()).getQueryParameter("before");
                            return Observable.just(
                                    conversationsResponse)
                                    .zipWith(
                                            getConversationsRequest(before)
                                                    .subscribeOn(mNetworkScheduler)
                                                    .observeOn(mUiScheduler),
                                            new Func2<ConversationsResponse, ConversationsResponse, ConversationsResponse>() {
                                                @Override
                                                public ConversationsResponse call(ConversationsResponse conversationsResponse, ConversationsResponse newResponse) {
                                                    return new ConversationsResponse(newResponse.getNext(),
                                                            newResponse.getPrevious(), ImmutableList.copyOf(Iterables.concat(
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

    @NonNull
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final Context mContext;
    private final UserPreferences mUserPreferences;
    private final PusherHelper mPusherHelper;
    private final boolean isMyConversationsList;
    private final PublishSubject<Object> requestSubject = PublishSubject.create();
    private Listener mListener;
    private Subscription mSubscription;
    private boolean showProgress = true;
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();

    @Inject
    public ConversationsPresenter(@NonNull ApiService apiService,
                                  @NetworkScheduler Scheduler networkScheduler,
                                  @UiScheduler Scheduler uiScheduler,
                                  @ForActivity Context context,
                                  UserPreferences userPreferences,
                                  PusherHelper pusherHelper,
                                  boolean isMyConversationsList) {
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mContext = context;
        mUserPreferences = userPreferences;
        mPusherHelper = pusherHelper;
        this.isMyConversationsList = isMyConversationsList;
    }

    private Observable<ConversationsResponse> getConversationsRequest(@Nullable String before) {
        if (isMyConversationsList) {
            return mApiService.getConversations(before, PAGE_SIZE);
        } else {
            return mApiService.publicChats(before, PAGE_SIZE);
        }
    }

    public void register(@NonNull final Listener listener) {
        mListener = listener;

        mListener.showProgress(showProgress);

        final Observable<PusherMessage> newMessageObservable = mPusherHelper
                .getNewMessagesObservable()
                .filter(new Func1<PusherMessage, Boolean>() {
                    @Override
                    public Boolean call(PusherMessage pusherMessage) {
                        return isMyConversationsList;
                    }
                })
                .observeOn(mUiScheduler);

        final Observable<Map<String, Conversation>> requestObservable = requestSubject
                .startWith(new Object())
                .lift(loadMoreOperator)
                .compose(MoreOperators.<ConversationsResponse>refresh(refreshSubject))
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .flatMap(new Func1<ConversationsResponse, Observable<Map<String, Conversation>>>() {
                    @Override
                    public Observable<Map<String, Conversation>> call(ConversationsResponse conversationsResponse) {
                        return Observable.from(conversationsResponse.getResults())
                                .toMap(new Func1<Conversation, String>() {
                                    @Override
                                    public String call(Conversation conversation) {
                                        return conversation.getId();
                                    }
                                });
                    }
                });

        final Observable<List<Conversation>> conversationsListObservable = requestObservable
                .switchMap(new Func1<Map<String, Conversation>, Observable<Map<String, Conversation>>>() {
                    @Override
                    public Observable<Map<String, Conversation>> call(final Map<String, Conversation> conversationsMap) {
                        return newMessageObservable.startWith((PusherMessage) null)
                                .scan(conversationsMap, new Func2<Map<String, Conversation>, PusherMessage, Map<String, Conversation>>() {
                                    @Override
                                    public Map<String, Conversation> call(Map<String, Conversation> conversationsMap,
                                                                          PusherMessage newMessage) {
                                        if (newMessage == null) {
                                            return conversationsMap;
                                        } else if (isNewConversation(conversationsMap, newMessage)) {
                                            refreshData();
                                            return conversationsMap;
                                        } else {
                                            return updateMapWithNewMessage(conversationsMap, newMessage);
                                        }
                                    }

                                    private boolean isNewConversation(Map<String, Conversation> conversationsMap,
                                                                      PusherMessage newMessage) {
                                        return !conversationsMap.containsKey(newMessage.getConversationId());
                                    }

                                    private Map<String, Conversation> updateMapWithNewMessage(Map<String, Conversation> conversationsMap,
                                                                                              PusherMessage newMessage) {
                                        final Conversation conversationToUpdate = conversationsMap.get(newMessage.getConversationId());
                                        final Conversation updatedConversation = conversationToUpdate
                                                .withUpdatedLastMessage(newMessage.getText(), newMessage.getCreatedAt());

                                        final Map<String, Conversation> newMap = new HashMap<>(conversationsMap);
                                        newMap.put(updatedConversation.getId(), updatedConversation);

                                        return ImmutableMap.copyOf(newMap);
                                    }
                                });
                    }
                })
                .map(new Func1<Map<String, Conversation>, List<Conversation>>() {
                    @Override
                    public List<Conversation> call(Map<String, Conversation> conversationsMap) {
                        return ImmutableList.copyOf(conversationsMap.values());
                    }
                });

        mSubscription = conversationsListObservable
                .subscribe(new Action1<List<Conversation>>() {
                    @Override
                    public void call(List<Conversation> conversations) {
                        showProgress = false;
                        mListener.showProgress(false);

                        if (conversations.isEmpty()) {
                            mListener.emptyList();
                        } else {
                            final List<Conversation> list = Lists.newArrayList(conversations);
                            Collections.sort(list, new Comparator<Conversation>() {
                                @Override
                                public int compare(Conversation lhs, Conversation rhs) {
                                    return lhs.getModifiedAt() >= rhs.getModifiedAt() ? -1 : 1;
                                }
                            });
                            final ImmutableList<BaseAdapterItem> items = ImmutableList.copyOf(
                                    Iterables.transform(
                                            list,
                                            new Function<Conversation, BaseAdapterItem>() {
                                                @Nullable
                                                @Override
                                                public BaseAdapterItem apply(@Nullable Conversation input) {
                                                    assert input != null;
                                                    return getConversationItem(input);
                                                }
                                            }));

                            mListener.setData(items);
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

    private void refreshData() {
        refreshSubject.onNext(null);
    }

    @NonNull
    private BaseAdapterItem getConversationItem(@NonNull Conversation conversation) {
        final Conversation.Display display = conversation.getDisplay();

        final String elapsedTime = DateUtils.getRelativeTimeSpanString(mContext, conversation.getModifiedAt() * 1000).toString();
        final User user = mUserPreferences.getUser();
        assert user != null;

        final boolean isUnread = conversation.getUnreadMessagesCount() > 0;

        return new ConversationAdapterItem(conversation.getId(), display.getTitle(), display.getSubTitle(),
                display.getLastMessageSummary(), elapsedTime, display.getImage(), isUnread, conversation.getType());
    }

    public Observer<Object> loadMoreObserver() {
        return requestSubject;
    }

    public void unregister() {
        mListener = null;
        mSubscription.unsubscribe();
    }

    public interface Listener {

        void emptyList();

        void showProgress(boolean show);

        void setData(@NonNull List<BaseAdapterItem> items);

        void error();

        void onItemClicked(@NonNull String id, boolean isPublicChat);
    }

    public class ConversationAdapterItem extends BaseNoIDAdapterItem {
        private final String id;
        private final String title;
        private final String subTitle;
        private final String message;
        private final String time;
        private final String image;
        private final boolean mIsUnread;
        private final String conversationType;

        public ConversationAdapterItem(String id,
                                       String title,
                                       String subTitle,
                                       String lastMessage,
                                       String time,
                                       String image,
                                       boolean isUnread,
                                       String conversationType) {
            this.id = id;
            this.title = title;
            this.subTitle = subTitle;
            this.message = lastMessage;
            this.time = time;
            this.image = image;
            mIsUnread = isUnread;
            this.conversationType = conversationType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ConversationAdapterItem that = (ConversationAdapterItem) o;

            if (mIsUnread != that.mIsUnread) return false;
            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (title != null ? !title.equals(that.title) : that.title != null) return false;
            if (subTitle != null ? !subTitle.equals(that.subTitle) : that.subTitle != null)
                return false;
            if (message != null ? !message.equals(that.message) : that.message != null)
                return false;
            if (time != null ? !time.equals(that.time) : that.time != null) return false;
            if (image != null ? !image.equals(that.image) : that.image != null) return false;
            return conversationType != null ? conversationType.equals(that.conversationType) : that.conversationType == null;

        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (title != null ? title.hashCode() : 0);
            result = 31 * result + (subTitle != null ? subTitle.hashCode() : 0);
            result = 31 * result + (message != null ? message.hashCode() : 0);
            result = 31 * result + (time != null ? time.hashCode() : 0);
            result = 31 * result + (image != null ? image.hashCode() : 0);
            result = 31 * result + (mIsUnread ? 1 : 0);
            result = 31 * result + (conversationType != null ? conversationType.hashCode() : 0);
            return result;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ConversationAdapterItem && ((ConversationAdapterItem) item).getId().equals(id);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return equals(item);
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public String getMessage() {
            return message;
        }

        public String getTime() {
            return time;
        }

        public String getImage() {
            return image;
        }

        public boolean isUnread() {
            return mIsUnread;
        }

        public boolean isShoutChat() {
            return Conversation.ABOUT_SHOUT_TYPE.equals(conversationType);
        }

        public boolean isPublicChat() {
            return Conversation.PUBLIC_CHAT_TYPE.equals(conversationType);
        }

        public void click() {
            mListener.onItemClicked(id, isPublicChat());
        }
    }


}
