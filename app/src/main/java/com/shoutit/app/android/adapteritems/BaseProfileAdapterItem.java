package com.shoutit.app.android.adapteritems;

import com.shoutit.app.android.api.model.BaseProfile;

import javax.annotation.Nonnull;

import rx.Observer;

public abstract class BaseProfileAdapterItem extends BaseNoIDAdapterItem {

    @Nonnull
    private final BaseProfile profile;
    @Nonnull
    private final Observer<BaseProfile> profileListenedObserver;
    @Nonnull
    private final Observer<Object> actionOnlyForLoggedInUsers;
    private final boolean isNormalUser;
    private final boolean isProfileMine;

    public BaseProfileAdapterItem(@Nonnull BaseProfile profile,
                                  @Nonnull Observer<BaseProfile> profileListenedObserver,
                                  @Nonnull Observer<Object> actionOnlyForLoggedInUsers,
                                  boolean isNormalUser,
                                  boolean isProfileMine) {
        this.profile = profile;
        this.profileListenedObserver = profileListenedObserver;
        this.actionOnlyForLoggedInUsers = actionOnlyForLoggedInUsers;
        this.isNormalUser = isNormalUser;
        this.isProfileMine = isProfileMine;
    }

    public abstract void openProfile();

    public void onProfileListened() {
        profileListenedObserver.onNext(profile);
    }

    @Nonnull
    public BaseProfile getProfile() {
        return profile;
    }

    public boolean isNormalUser() {
        return isNormalUser;
    }

    public void onActionOnlyForLoggedInUsers() {
        actionOnlyForLoggedInUsers.onNext(null);
    }

    public boolean isProfileMine() {
        return isProfileMine;
    }
}