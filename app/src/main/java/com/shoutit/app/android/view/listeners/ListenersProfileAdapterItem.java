package com.shoutit.app.android.view.listeners;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseProfileAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;

import javax.annotation.Nonnull;

import rx.Observer;

public class ListenersProfileAdapterItem extends BaseProfileAdapterItem {

    @Nonnull
    private final BaseProfile profile;
    @Nonnull
    private final Observer<String> openProfileObserver;


    public ListenersProfileAdapterItem(@Nonnull BaseProfile profile,
                                       @Nonnull Observer<String> openProfileObserver,
                                       @Nonnull Observer<BaseProfile> profileListenedObserver) {
        super(profile, profileListenedObserver);
        this.profile = profile;
        this.openProfileObserver = openProfileObserver;
    }

    @Override
    public void openProfile() {
        openProfileObserver.onNext(profile.getUsername());
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ListenersProfileAdapterItem &&
                profile.getUsername().equals(((ListenersProfileAdapterItem) item).profile.getUsername());
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ListenersProfileAdapterItem &&
                profile.equals(((ListenersProfileAdapterItem) item).profile);
    }
}