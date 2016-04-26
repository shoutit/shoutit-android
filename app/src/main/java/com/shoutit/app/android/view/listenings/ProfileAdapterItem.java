package com.shoutit.app.android.view.listenings;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.ProfileType;

import javax.annotation.Nonnull;

import rx.Observer;

public class ProfileAdapterItem extends BaseNoIDAdapterItem {

    @Nonnull
    private final ProfileType profile;
    @Nonnull
    private final Observer<String> openProfileObserver;

    public ProfileAdapterItem(@Nonnull ProfileType profile,
                              @Nonnull Observer<String> openProfileObserver) {
        this.profile = profile;
        this.openProfileObserver = openProfileObserver;
    }

    public void openProfile() {
        openProfileObserver.onNext(profile.getUsername());
    }

    @Nonnull
    public ProfileType getProfile() {
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