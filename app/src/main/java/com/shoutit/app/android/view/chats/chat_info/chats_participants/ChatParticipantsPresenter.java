package com.shoutit.app.android.view.chats.chat_info.chats_participants;

import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ConversationDetails;
import com.shoutit.app.android.api.model.ConversationProfile;
import com.shoutit.app.android.api.model.ProfileRequest;
import com.shoutit.app.android.api.model.ProfileType;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class ChatParticipantsPresenter {

    private final ApiService mApiService;
    private final String mConversationId;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private final String mId;
    private Listener mListener;

    @Inject
    public ChatParticipantsPresenter(@NonNull ApiService apiService,
                                     @NonNull String conversationId,
                                     @NetworkScheduler Scheduler networkScheduler,
                                     @UiScheduler Scheduler uiScheduler,
                                     UserPreferences userPreferences) {
        mApiService = apiService;
        mConversationId = conversationId;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;

        mId = userPreferences.getUserId();
    }

    public void register(Listener listener) {
        mListener = listener;
        getConversation();
    }

    private void getConversation() {
        mListener.showProgress(true);
        mCompositeSubscription.add(mApiService.getConversation(mConversationId)
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(new Action1<ConversationDetails>() {
                    @Override
                    public void call(final ConversationDetails conversation) {
                        final boolean isUserAdmin = conversation.getAdmins().contains(mId);

                        final List<ConversationProfile> profiles = conversation.getProfiles();
                        final List<BaseAdapterItem> profileItems = getBaseAdapterItems(conversation, isUserAdmin, profiles);
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

    private ImmutableList<BaseAdapterItem> getBaseAdapterItems(final ConversationDetails conversation, final boolean isUserAdmin, List<ConversationProfile> profiles) {
        return ImmutableList.copyOf(Iterables.transform(profiles, new Function<ConversationProfile, BaseAdapterItem>() {
            @Nullable
            @Override
            public BaseAdapterItem apply(@Nullable final ConversationProfile profile) {
                return getProfileItem(profile, conversation, new ProfileItem.OnItemClicked() {
                    @Override
                    public void onItemClicked(String id, boolean isBlocked, boolean isAdmin, String name) {
                        assert profile != null;
                        final boolean isPage = profile.getType().equals(ProfileType.PAGE);
                        if (!profile.getId().equals(mId) && isUserAdmin) {
                            mListener.showDialog(id, isBlocked, isAdmin, isPage, name, profile.getUsername());
                        } else {
                            mListener.showProfile(profile.getUsername(), isPage);
                        }
                    }
                });
            }
        }));
    }

    @NonNull
    private ProfileItem getProfileItem(@Nullable ConversationProfile input, ConversationDetails conversation, ProfileItem.OnItemClicked onItemClicked) {
        assert input != null;

        final String id = input.getId();

        final boolean isAdmin = conversation.getAdmins().contains(id);
        final boolean isBlocked = conversation.getBlocked().contains(id);

        return new ProfileItem(id, input.getName(), input.getImage(), isAdmin, isBlocked, onItemClicked);
    }

    public void blockAction(String id, boolean markAsBlocked) {
        if (markAsBlocked) {
            blockUser(id);
        } else {
            unblockUser(id);
        }
    }

    private void unblockUser(String id) {
        userAction(mApiService.unblockProfile(mConversationId, new ProfileRequest(id)));
    }

    private void blockUser(String id) {
        userAction(mApiService.blockProfile(mConversationId, new ProfileRequest(id)));
    }

    public void adminAction(String id) {
        userAction(mApiService.promoteAdmin(mConversationId, new ProfileRequest(id)));
    }

    public void removeUser(String id) {
        userAction(mApiService.removeProfile(mConversationId, new ProfileRequest(id)));
    }

    private void userAction(@NonNull Observable<ResponseBody> observable) {
        mListener.showProgress(true);
        mCompositeSubscription.add(observable
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        getConversation();
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

    public void showProfile(@NonNull String userName, boolean isPage) {
        mListener.showProfile(userName, isPage);
    }

    public interface Listener {

        void error();

        void setData(List<BaseAdapterItem> profileItems);

        void showProgress(boolean show);

        void showDialog(String id, boolean isBlocked, boolean isAdmin, boolean isPage, String name, String userName);

        void showProfile(String username, boolean isPage);
    }
}
