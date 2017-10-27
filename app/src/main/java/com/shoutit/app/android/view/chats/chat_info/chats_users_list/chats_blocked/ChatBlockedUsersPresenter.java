package com.shoutit.app.android.view.chats.chat_info.chats_users_list.chats_blocked;

import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.api.model.BlockedProfilesResposne;
import com.shoutit.app.android.api.model.ConversationProfile;
import com.shoutit.app.android.api.model.ProfileRequest;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.ChatListProfileItem;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Scheduler;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class ChatBlockedUsersPresenter {

    private final ApiService mApiService;
    private final String mConversationId;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private Listener mListener;

    @Inject
    public ChatBlockedUsersPresenter(@NonNull ApiService apiService,
                                     @NonNull String conversationId,
                                     @NetworkScheduler Scheduler networkScheduler,
                                     @UiScheduler Scheduler uiScheduler) {
        mApiService = apiService;
        mConversationId = conversationId;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
    }

    public void register(Listener listener) {
        mListener = listener;
        getConversation();
    }

    private void getConversation() {
        mListener.showProgress(true);
        mCompositeSubscription.add(mApiService.getBlockedProfiles(mConversationId)
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(new Action1<BlockedProfilesResposne>() {
                    @Override
                    public void call(final BlockedProfilesResposne blockedProfilesResposne) {
                        final List<ConversationProfile> profiles = blockedProfilesResposne.getProfiles();
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

    private List<BaseAdapterItem> getProfileItems(List<ConversationProfile> profiles) {
        return ImmutableList.copyOf(Iterables.transform(profiles, new Function<ConversationProfile, BaseAdapterItem>() {
            @Nullable
            @Override
            public BaseAdapterItem apply(@Nullable ConversationProfile profile) {
                assert profile != null;
                return new ChatListProfileItem(profile.getId(), profile.getName(), profile.getImage(), new ChatListProfileItem.OnItemClicked() {
                    @Override
                    public void onItemClicked(String id, String name) {
                        mListener.showDialog(id, name, profile.getUsername(), profile.isPage());
                    }
                });
            }
        }));
    }

    public void unblockUser(String id) {
        mListener.showProgress(true);
        mCompositeSubscription.add(mApiService.unblockProfile(mConversationId, new ProfileRequest(id))
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(new Action1<ApiMessageResponse>() {
                    @Override
                    public void call(ApiMessageResponse apiMessageResponse) {
                        getConversation();
                        mListener.showApiMessage(apiMessageResponse);
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

    public void showProfile(String userName, boolean isPage) {
        mListener.showProfile(userName, isPage);
    }

    public void showUnblockConfirmDialog(String id, String name) {
        mListener.showUnblockConfirmDialog(id, name);
    }

    public interface Listener {

        void error();

        void setData(List<BaseAdapterItem> profileItems);

        void showProgress(boolean show);

        void showDialog(String id, String name, String userName, boolean isPage);

        void showProfile(String userName, boolean isPage);

        void showUnblockConfirmDialog(String id, String name);

        void showApiMessage(ApiMessageResponse apiMessageResponse);
    }
}
