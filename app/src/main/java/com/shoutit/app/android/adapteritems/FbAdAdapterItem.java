package com.shoutit.app.android.adapteritems;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.facebook.ads.NativeAd;

import javax.annotation.Nonnull;

public class FbAdAdapterItem extends BaseNoIDAdapterItem {

    @Nonnull
    private final NativeAd ad;

    public FbAdAdapterItem(@Nonnull NativeAd ad) {
        this.ad = ad;
    }

    @Nonnull
    public NativeAd getAd() {
        return ad;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
        return baseAdapterItem instanceof FbAdAdapterItem;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
        return false;
    }
}