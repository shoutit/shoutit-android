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
    private final Observer<BaseProfile> profileSelectedObserver;
    @Nonnull
    private final Observer<BaseProfile> profileListenedObserver;


    public ProfileListAdapterItem(@Nonnull BaseProfile profile,
                                  @Nonnull Observer<BaseProfile> profileSelectedObserver,
                                  @Nonnull Observer<BaseProfile> profileListenedObserver,
                                  @Nonnull Observer<Object> actionOnlyForLoggedInUsers,
                                  boolean isNormalUser,
                                  boolean isProfileMine) {
        super(profile, profileListenedObserver, actionOnlyForLoggedInUsers, isNormalUser, isProfileMine);
        this.profile = profile;
        this.profileSelectedObserver = profileSelectedObserver;
        this.profileListenedObserver = profileListenedObserver;
    }

    @Override
    public void openProfile() {
        profileSelectedObserver.onNext(profile);
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