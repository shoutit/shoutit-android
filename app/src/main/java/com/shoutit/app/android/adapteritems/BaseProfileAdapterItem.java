package com.shoutit.app.android.adapteritems;

import com.shoutit.app.android.api.model.BaseProfile;

import javax.annotation.Nonnull;

import rx.Observer;

public abstract class BaseProfileAdapterItem extends BaseNoIDAdapterItem {

    @Nonnull
    private final BaseProfile profile;
    @Nonnull
    private final Observer<BaseProfile> profileListenedObserver;

    public BaseProfileAdapterItem(@Nonnull BaseProfile profile,
                                        @Nonnull Observer<BaseProfile> profileListenedObserver) {
        this.profile = profile;
        this.profileListenedObserver = profileListenedObserver;
    }

    public abstract void openProfile();

    public void onProfileListened() {
        profileListenedObserver.onNext(profile);
    }

    @Nonnull
    public BaseProfile getProfile() {
        return profile;
    }

}