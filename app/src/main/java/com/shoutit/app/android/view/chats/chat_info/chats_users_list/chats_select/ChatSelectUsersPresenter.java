package com.shoutit.app.android.view.chats.chat_info.chats_users_list.chats_select;

import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfileRequest;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.ChatListProfileItem;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Scheduler;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class ChatSelectUsersPresenter {

    private final ApiService mApiService;
    private final String mConversationId;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private Listener mListener;
    private final String mId;

    @Inject
    public ChatSelectUsersPresenter(@NonNull ApiService apiService,
                                    @NonNull String conversationId,
                                    @NetworkScheduler Scheduler networkScheduler,
                                    @UiScheduler Scheduler uiScheduler,
                                    UserPreferences userPreferences) {
        mApiService = apiService;
        mConversationId = conversationId;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;

        final BaseProfile user = userPreferences.getUserOrPage();
        assert user != null;
        mId = user.getUsername();
    }

    public void register(Listener listener) {
        mListener = listener;
        getConversation();
    }

    private void getConversation() {
        mListener.showProgress(true);
        mCompositeSubscription.add(mApiService.listeners(mId, 1, 20) // TODO paging
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(new Action1<ProfilesListResponse>() {
                    @Override
                    public void call(final ProfilesListResponse listenersResponse) {
                        final List<BaseProfile> profiles = listenersResponse.getResults();
                        final List<BaseAdapterItem> profileItems = getProfileItems(profiles);

                        mListener.setData(profileItems);
                        mListener.showProgress(false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mListener.error();
                        mListener.showProgress(false);
                    }
                }));
    }

    private List<BaseAdapterItem> getProfileItems(List<BaseProfile> profiles) {
        return ImmutableList.copyOf(Iterables.transform(profiles, new Function<BaseProfile, BaseAdapterItem>() {
            @Nullable
            @Override
            public BaseAdapterItem apply(@Nullable BaseProfile input) {
                assert input != null;
                return new ChatListProfileItem(input.getId(), input.getName(), input.getImage(), new ChatListProfileItem.OnItemClicked() {
                    @Override
                    public void onItemClicked(String id, String name) {
                        mListener.showDialog(id, name);
                    }
                });
            }
        }));
    }

    public void addProfile(String id) {
        mListener.showProgress(true);
        mCompositeSubscription.add(mApiService.addProfile(mConversationId, new ProfileRequest(id))
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        mListener.finishScreen();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mListener.showProgress(false);
                        mListener.error();
                    }
                }));
    }

    public void unregister() {
        mCompositeSubscription.unsubscribe();
    }

    public interface Listener {

        void error();

        void setData(List<BaseAdapterItem> profileItems);

        void showProgress(boolean show);

        void showDialog(String id, String name);

        void finishScreen();
    }
}
