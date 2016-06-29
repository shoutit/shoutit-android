package com.shoutit.app.android.view.listenings;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.ProfilesHelper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;

public class ListenUserOrPageHalfPresenter extends ListeningHalfPresenter {

    @Nonnull
    private final ListeningsPresenter.ListeningsType listeningsType;

    public ListenUserOrPageHalfPresenter(@Nonnull ApiService apiService,
                                         @NetworkScheduler Scheduler networkScheduler,
                                         @UiScheduler Scheduler uiScheduler,
                                         @Nonnull final ListeningsPresenter.ListeningsType listeningsType) {
        super(apiService, networkScheduler, uiScheduler);
        this.listeningsType = listeningsType;
    }

    @Nonnull
    @Override
    public Observable<ResponseBody> getListenRequest(@Nonnull BaseProfile baseProfile) {
        final String userNameOrTagName = getProfileId(baseProfile);

        if (listeningsType.equals(ListeningsPresenter.ListeningsType.INTERESTS)) {
            return apiService.listenTag(userNameOrTagName);
        } else {
            return apiService.listenProfile(userNameOrTagName);
        }
    }

    @Nonnull
    @Override
    public Observable<ResponseBody> getUnlistenRequest(@Nonnull BaseProfile baseProfile) {
        final String userOrTagName = getProfileId(baseProfile);

        if (listeningsType.equals(ListeningsPresenter.ListeningsType.INTERESTS)) {
            return apiService.unlistenTag(userOrTagName);
        } else {
            return apiService.unlistenProfile(userOrTagName);
        }
    }

    @Nonnull
    private String getProfileId(BaseProfile baseProfile) {
        if (listeningsType.equals(ListeningsPresenter.ListeningsType.INTERESTS)) {
            return baseProfile.getName();
        } else {
            return baseProfile.getUsername();
        }
    }

    @Override
    protected ProfilesListResponse updateResponseWithListenedProfiles(@Nonnull ProfilesHelper.ProfileToListenWithLastResponse profileToListenWithLastResponse) {
        final ProfilesListResponse response = profileToListenWithLastResponse.getResponse();

        final List<BaseProfile> profiles = response.getResults();
        final String profileToUpdateId = getProfileId(profileToListenWithLastResponse.getProfile());

        for (int i = 0; i < profiles.size(); i++) {
            if (getProfileId(profiles.get(i)).equals(profileToUpdateId)) {
                final BaseProfile profileToUpdate = profiles.get(i);
                final BaseProfile updatedProfile = profileToUpdate.getListenedProfile();

                final List<BaseProfile> updatedProfiles = new ArrayList<>(profiles);
                updatedProfiles.set(i, updatedProfile);

                return new ProfilesListResponse(response.getCount(), response.getNext(),
                        response.getPrevious(), updatedProfiles);
            }
        }

        return response;
    }

}
