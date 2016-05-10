package com.shoutit.app.android.view.listenings;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.api.model.ProfileType;

import javax.annotation.Nonnull;

import rx.Observer;

public class ProfileAdapterItem extends BaseNoIDAdapterItem {

    @Nonnull
    private final BaseProfile profile;
    @Nonnull
    private final Observer<String> openProfileObserver;
    @Nonnull
    private final Observer<ListeningsPresenter.ProfileToListenWithLastResponse> profileListenedObserver;
    @Nonnull
    private final ListeningResponse lastResponse;


    public ProfileAdapterItem(@Nonnull BaseProfile profile,
                              @Nonnull Observer<String> openProfileObserver,
                              @Nonnull Observer<ListeningsPresenter.ProfileToListenWithLastResponse> profileListenedObserver,
                              @Nonnull ListeningResponse lastResponse) {
        this.profile = profile;
        this.openProfileObserver = openProfileObserver;
        this.profileListenedObserver = profileListenedObserver;
        this.lastResponse = lastResponse;
    }

    public void openProfile() {
        openProfileObserver.onNext(profile.getUsername());
    }

    public void onProfileListened() {
        profileListenedObserver.onNext(new ListeningsPresenter.ProfileToListenWithLastResponse(profile, lastResponse));
    }

    @Nonnull
    public BaseProfile getProfile() {
        return profile;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ProfileAdapterItem &&
                profile.getUsername().equals(((ProfileAdapterItem) item).profile.getUsername());
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ProfileAdapterItem &&
                profile.equals(((ProfileAdapterItem) item).profile);
    }
}