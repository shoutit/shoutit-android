package com.shoutit.app.android.view.conversations;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.ConversationsResponse;
import com.shoutit.app.android.api.model.Message;
import com.shoutit.app.android.api.model.MessageAttachment;
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
        final Observable<HashMap<String, Conversation>> mapObservable = mPusherHelper.getNewMessagesObservable()
                .flatMap(new Func1<PusherMessage, Observable<Conversation>>() {
                    @Override
                    public Observable<Conversation> call(PusherMessage pusherMessage) {
                        return mApiService.getConversation(pusherMessage.getConversationId());
                    }
                })
                .observeOn(mUiScheduler)
                .filter(new Func1<Conversation, Boolean>() {
                    @Override
                    public Boolean call(Conversation conversation) {
                        return isMyConversationsList;
                    }
                })
                .scan(Maps.<String, Conversation>newHashMap(), new Func2<HashMap<String, Conversation>, Conversation, HashMap<String, Conversation>>() {
                    @Override
                    public HashMap<String, Conversation> call(HashMap<String, Conversation> map, Conversation pusherMessage) {
                        map.put(pusherMessage.getId(), pusherMessage);
                        return map;
                    }
                });

        mListener = listener;

        mListener.showProgress(showProgress);

        final Observable<List<Conversation>> listObservable = requestSubject
                .startWith(new Object())
                .lift(loadMoreOperator)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .map(new Func1<ConversationsResponse, List<Conversation>>() {
                    @Override
                    public List<Conversation> call(ConversationsResponse conversationsResponse) {
                        return conversationsResponse.getResults();
                    }
                });


        mSubscription = Observable.combineLatest(listObservable, mapObservable,
                new Func2<List<Conversation>, Map<String, Conversation>, List<Conversation>>() {
                    @Override
                    public List<Conversation> call(List<Conversation> conversations, Map<String, Conversation> map) {
                        for (Conversation conversation : conversations) {
                            final boolean containsKey = map.containsKey(conversation.getId());
                            if (!containsKey) {
                                // MAP IS HASHMAP
                                map.put(conversation.getId(), conversation);
                            }
                        }

                        return ImmutableList.copyOf(Iterables.filter(Iterables.transform(map.entrySet(), new Function<Map.Entry<String, Conversation>, Conversation>() {
                            @Nullable
                            @Override
                            public Conversation apply(@Nullable Map.Entry<String, Conversation> input) {
                                assert input != null;
                                return input.getValue();
                            }
                        }), new Predicate<Conversation>() {
                            @Override
                            public boolean apply(@Nullable Conversation conversation) {
                                assert conversation != null;
                                return conversation.isPublicChat() || conversation.getProfiles().size() > 1;
                            }
                        }));
                    }
                })
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
                                    return lhs.getLastMessage().getCreatedAt() >= rhs.getLastMessage().getCreatedAt() ? -1 : 1;
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

    @NonNull
    private BaseAdapterItem getConversationItem(@NonNull Conversation input) {
        final Message lastMessage = input.getLastMessage();
        final Conversation.DisplayData displayData = input.getDisplay();

        final String message = getMessageString(lastMessage);
        final String elapsedTime = DateUtils.getRelativeTimeSpanString(mContext, lastMessage.getCreatedAt() * 1000).toString();
        final User user = mUserPreferences.getUser();
        assert user != null;

        final boolean isUnread = input.getUnreadMessagesCount() > 0;

        return new ConversationAdapterItem(input.getId(), displayData.getTitle(), displayData.getSubTitle(),
                message, elapsedTime, displayData.getImage(), isUnread, input.getType());
    }

    private String getMessageString(Message lastMessage) {
        final List<MessageAttachment> attachments = lastMessage.getAttachments();
        if (attachments == null || attachments.isEmpty()) {
            return lastMessage.getText();
        } else {
            final MessageAttachment messageAttachment = attachments.get(0);
            if (MessageAttachment.ATTACHMENT_TYPE_LOCATION.equals(messageAttachment.getType())) {
                return mContext.getString(R.string.chats_attatchments_location);
            } else if (MessageAttachment.ATTACHMENT_TYPE_SHOUT.equals(messageAttachment.getType())) {
                return mContext.getString(R.string.chats_attatchments_shout);
            } else if (MessageAttachment.ATTACHMENT_TYPE_MEDIA.equals(messageAttachment.getType())) {
                return mContext.getString(R.string.chats_attatchments_media);
            } else if (MessageAttachment.ATTACHMENT_TYPE_PROFILE.equals(messageAttachment.getType())) {
                return mContext.getString(R.string.chats_attatchments_profile);
            } else {
                throw new RuntimeException(messageAttachment.getType() + " : unknown type");
            }
        }
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

        void onItemClicked(@NonNull String id, boolean shoutChat, boolean isPublicChat);
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
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
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
            mListener.onItemClicked(id, isShoutChat(), isPublicChat());
        }
    }


}
