package com.shoutit.app.android.adapteritems;

import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.utils.PriceUtils;

import javax.annotation.Nonnull;

import rx.Observer;

public class BaseShoutAdapterItem implements BaseAdapterItem {

    public static class PromotionInfo {

        private final int bgColor;
        private final int color;
        private final String label;

        public PromotionInfo(@ColorInt int bgColor, @ColorInt int color, @NonNull String label) {
            this.bgColor = bgColor;
            this.color = color;
            this.label = label;
        }

        public int getBgColor() {
            return bgColor;
        }

        public int getColor() {
            return color;
        }

        public String getLabel() {
            return label;
        }
    }

    @Nonnull
    private final Shout shout;
    @Nonnull
    private final Resources resources;
    @Nonnull
    private final Observer<String> shoutSelectedObserver;
    @Nullable
    private final PromotionInfo mPromotionInfo;

    public BaseShoutAdapterItem(@Nonnull Shout shout, @Nonnull Resources resources,
                            @Nonnull Observer<String> shoutSelectedObserver,
                            @Nullable PromotionInfo promotionInfo) {
        this.shout = shout;
        this.resources = resources;
        this.shoutSelectedObserver = shoutSelectedObserver;
        this.mPromotionInfo = promotionInfo;
    }

    @Override
    public long adapterId() {
        return BaseAdapterItem.NO_ID;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof BaseShoutAdapterItem &&
                shout.getId().equals(((BaseShoutAdapterItem) item).shout.getId());
    }

    public boolean isPromoted() {
        return mPromotionInfo != null;
    }

    public int getBgColor() {
        assert mPromotionInfo != null;
        return mPromotionInfo.getBgColor();
    }

    public int getColor() {
        assert mPromotionInfo != null;
        return mPromotionInfo.getColor();
    }

    public String getLabel() {
        assert mPromotionInfo != null;
        return mPromotionInfo.getLabel();
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return this.equals(item);
    }

    @Nonnull
    public Shout getShout() {
        return shout;
    }

    @Nullable
    public String getShoutPrice() {
        final Long price = shout.getPrice();
        if (price == null) {
            return null;
        } else {
            return PriceUtils.formatPriceWithCurrency(shout.getPrice(), resources, shout.getCurrency());
        }
    }

    public void onShoutSelected() {
        shoutSelectedObserver.onNext(shout.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BaseShoutAdapterItem that = (BaseShoutAdapterItem) o;

        return shout.equals(that.shout);

    }

    @Override
    public int hashCode() {
        return shout.hashCode();
    }
}

