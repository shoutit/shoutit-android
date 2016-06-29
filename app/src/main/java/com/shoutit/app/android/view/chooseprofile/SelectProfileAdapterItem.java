package com.shoutit.app.android.view.chooseprofile;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;

import javax.annotation.Nonnull;

import rx.Observer;

public class SelectProfileAdapterItem extends BaseNoIDAdapterItem {

    @Nonnull
    private final Observer<BaseProfile> profileSelectedObserver;
    @Nonnull
    private final BaseProfile baseProfile;

    public SelectProfileAdapterItem(@Nonnull Observer<BaseProfile> profileSelectedObserver,
                                    @Nonnull BaseProfile baseProfile) {
        this.profileSelectedObserver = profileSelectedObserver;
        this.baseProfile = baseProfile;
    }

    @Nonnull
    public BaseProfile getProfile() {
        return baseProfile;
    }

    public void onProfileSelected() {
        profileSelectedObserver.onNext(baseProfile);
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof SelectProfileAdapterItem &&
                baseProfile.getId().equals(((SelectProfileAdapterItem) item).baseProfile.getId());
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return baseProfile.equals(((SelectProfileAdapterItem) item).baseProfile);
    }
}
