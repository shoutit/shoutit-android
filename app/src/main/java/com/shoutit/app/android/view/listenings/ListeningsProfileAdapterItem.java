package com.shoutit.app.android.view.listenings;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseProfileAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;

import javax.annotation.Nonnull;

import rx.Observer;

public class ListeningsProfileAdapterItem extends BaseProfileAdapterItem {

    @Nonnull
    private final BaseProfile profile;
    @Nonnull
    private final Observer<BaseProfile> openProfileObserver;


    public ListeningsProfileAdapterItem(@Nonnull BaseProfile profile,
                                        @Nonnull Observer<BaseProfile> openProfileObserver,
                                        @Nonnull Observer<BaseProfile> profileListenedObserver,
                                        @Nonnull Observer<Object> actionOnlyForLoggedInUsers,
                                        boolean isNormalUser,
                                        boolean isProfileMine) {
        super(profile, profileListenedObserver, actionOnlyForLoggedInUsers, isNormalUser, isProfileMine);
        this.profile = profile;
        this.openProfileObserver = openProfileObserver;
    }

    @Override
    public void openProfile() {
        openProfileObserver.onNext(profile);
    }

    private String getProfileId() {
        return profile.getUsername();
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ListeningsProfileAdapterItem &&
                getProfileId().equals(((ListeningsProfileAdapterItem) item).getProfileId());
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ListeningsProfileAdapterItem &&
                profile.equals(((ListeningsProfileAdapterItem) item).profile);
    }
}