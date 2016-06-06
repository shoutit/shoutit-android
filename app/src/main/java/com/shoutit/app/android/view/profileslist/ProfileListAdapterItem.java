package com.shoutit.app.android.view.profileslist;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseProfileAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;

import javax.annotation.Nonnull;

import rx.Observer;

public class ProfileListAdapterItem extends BaseProfileAdapterItem {

    @Nonnull
    private final BaseProfile profile;
    @Nonnull
    private final Observer<String> openProfileObserver;
    @Nonnull
    private final Observer<BaseProfile> profileListenedObserver;


    public ProfileListAdapterItem(@Nonnull BaseProfile profile,
                                  @Nonnull Observer<String> openProfileObserver,
                                  @Nonnull Observer<BaseProfile> profileListenedObserver,
                                  @Nonnull Observer<Object> actionOnlyForLoggedInUsers,
                                  boolean isNormalUser,
                                  boolean isProfileMine) {
        super(profile, profileListenedObserver, actionOnlyForLoggedInUsers, isNormalUser, isProfileMine);
        this.profile = profile;
        this.openProfileObserver = openProfileObserver;
        this.profileListenedObserver = profileListenedObserver;
    }

    @Override
    public void openProfile() {
        openProfileObserver.onNext(profile.getUsername());
    }

    public void onProfileListened() {
        profileListenedObserver.onNext(profile);
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ProfileListAdapterItem &&
                profile.getUsername().equals(((ProfileListAdapterItem) item).getProfile().getUsername());
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ProfileListAdapterItem &&
                profile.equals(((ProfileListAdapterItem) item).getProfile());
    }
}