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
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.ConversationProfile;
import com.shoutit.app.android.api.model.ConversationsResponse;
import com.shoutit.app.android.api.model.Message;
import com.shoutit.app.android.api.model.MessageAttachment;
import com.shoutit.app.android.api.model.PusherMessage;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PusherHelper;

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

    final OperatorMergeNextToken<ConversationsResponse, Object> loadMoreOperator =
            OperatorMergeNextToken.create(new Func1<ConversationsResponse, Observable<ConversationsResponse>>() {

                @Override
                public Observable<ConversationsResponse> call(ConversationsResponse conversationsResponse) {
                    if (conversationsResponse == null || conversationsResponse.getNext() != null) {
                        if (conversationsResponse == null) {
                            return mApiService.getConversations()
                                    .subscribeOn(mNetworkScheduler)
                                    .observeOn(mUiScheduler);
                        } else {
                            final String after = Uri.parse(conversationsResponse.getNext()).getQueryParameter("after");
                            return Observable.just(
                                    conversationsResponse)
                                    .zipWith(
                                            mApiService.getConversations(after)
                                                    .subscribeOn(mNetworkScheduler)
                                                    .observeOn(mUiScheduler),
                                            new Func2<ConversationsResponse, ConversationsResponse, ConversationsResponse>() {
                                                @Override
                                                public ConversationsResponse call(ConversationsResponse conversationsResponse, ConversationsResponse newResponse) {
                                                    return new ConversationsResponse(newResponse.getNext(),
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

    @NonNull
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final Context mContext;
    private final UserPreferences mUserPreferences;
    private final PusherHelper mPusherHelper;
    private final PublishSubject<Object> requestSubject = PublishSubject.create();
    private Listener mListener;
    private Subscription mSubscription;
    private boolean showProgress;

    @Inject
    public ConversationsPresenter(@NonNull ApiService apiService,
                                  @NetworkScheduler Scheduler networkScheduler,
                                  @UiScheduler Scheduler uiScheduler,
                                  @ForActivity Context context,
                                  UserPreferences userPreferences,
                                  PusherHelper pusherHelper) {
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mContext = context;
        mUserPreferences = userPreferences;
        mPusherHelper = pusherHelper;
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
                            public boolean apply(@Nullable Conversation input) {
                                assert input != null;
                                return input.getProfiles().size() > 1;
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
        final List<ConversationProfile> profiles = input.getProfiles();

        final String message = getMessageString(lastMessage);
        final String elapsedTime = DateUtils.getRelativeTimeSpanString(mContext, lastMessage.getCreatedAt() * 1000).toString();
        final User user = mUserPreferences.getUser();
        assert user != null;
        final String chatWith = ConversationsUtils.getChatWithString(profiles, user.getId());
        final String image = getImage(profiles);

        if (Conversation.ABOUT_SHOUT_TYPE.equals(input.getType())) {
            return new ConversationShoutItem(input.getId(), input.getAbout().getTitle(), chatWith, message, elapsedTime, image);
        } else if (Conversation.CHAT_TYPE.equals(input.getType())) {
            return new ConversationChatItem(input.getId(), message, chatWith, elapsedTime, image);
        } else {
            throw new RuntimeException(input.getType() + " : unknown type");
        }
    }

    private String getImage(List<ConversationProfile> profiles) {
        final User user = mUserPreferences.getUser();
        assert user != null;
        final String id = user.getId();
        for (ConversationProfile profile : profiles) {
            if (!profile.getId().equals(id)) {
                return profile.getImage();
            }
        }
        return null;
    }

    private String getMessageString(Message lastMessage) {
        final List<MessageAttachment> attachments = lastMessage.getAttachments();
        if (attachments == null || attachments.isEmpty()) {
            return lastMessage.getText();
        } else {
            final MessageAttachment messageAttachment = attachments.get(0);
            if (MessageAttachment.ATTACHMENT_TYPE_LOCATION.equals(messageAttachment.getType())) {
                return "Location";
            } else if (MessageAttachment.ATTACHMENT_TYPE_SHOUT.equals(messageAttachment.getType())) {
                return "Shout";
            } else if (MessageAttachment.ATTACHMENT_TYPE_MEDIA.equals(messageAttachment.getType())) {
                return "Media";
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

        void onItemClicked(@NonNull String id, boolean shoutChat);
    }

    public class ConversationChatItem implements BaseAdapterItem {
        private final String id;
        private final String message;
        private final String user;
        private final String time;
        private final String image;

        public ConversationChatItem(String id, String message, String user, String time, String image) {
            this.id = id;
            this.message = message;
            this.user = user;
            this.time = time;
            this.image = image;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Override
        public long adapterId() {
            return 0;
        }

        public String getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public String getUser() {
            return user;
        }

        public String getTime() {
            return time;
        }

        public String getImage() {
            return image;
        }

        public void click() {
            mListener.onItemClicked(id, false);
        }
    }

    public class ConversationShoutItem implements BaseAdapterItem {
        private final String id;
        private final String shoutDescription;
        private final String userNames;
        private final String message;
        private final String time;
        private final String image;

        public ConversationShoutItem(String id, String shoutDescription, String userNames, String message, String time, String image) {
            this.id = id;
            this.shoutDescription = shoutDescription;
            this.userNames = userNames;
            this.message = message;
            this.time = time;
            this.image = image;
        }

        @Override
        public long adapterId() {
            return 0;
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

        public String getShoutDescription() {
            return shoutDescription;
        }

        public String getUserNames() {
            return userNames;
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

        public void click() {
            mListener.onItemClicked(id, true);
        }
    }


}
