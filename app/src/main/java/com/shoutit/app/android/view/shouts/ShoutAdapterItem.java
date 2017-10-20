package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.ResourcesHelper;

import javax.annotation.Nonnull;

import rx.Observer;

public class ShoutAdapterItem implements BaseAdapterItem {

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
    private final boolean isShoutOwner;
    private final boolean isNormalUser;
    @Nonnull
    private final Context context;
    @Nonnull
    private final Observer<String> shoutSelectedObserver;
    @Nullable
    private final PromotionInfo mPromotionInfo;

    public ShoutAdapterItem(@Nonnull Shout shout, boolean isShoutOwner,
                            boolean isNormalUser, @Nonnull Context context,
                            @Nonnull Observer<String> shoutSelectedObserver,
                            @Nullable PromotionInfo promotionInfo) {
        this.shout = shout;
        this.isShoutOwner = isShoutOwner;
        this.isNormalUser = isNormalUser;
        this.context = context;
        this.shoutSelectedObserver = shoutSelectedObserver;
        this.mPromotionInfo = promotionInfo;
    }

    @Override
    public long adapterId() {
        return BaseAdapterItem.NO_ID;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ShoutAdapterItem &&
                shout.getId().equals(((ShoutAdapterItem) item).shout.getId());
    }

    public boolean isShoutOwner() {
        return isShoutOwner;
    }

    public boolean isNormalUser() {
        return isNormalUser;
    }

    public boolean isPromoted(){
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
    public String getCategoryIconUrl() {
        if (shout.getCategory() != null) {
            return Strings.emptyToNull(shout.getCategory().getIcon());
        } else {
            return null;
        }
    }

    @Nullable
    public String getShoutPrice() {
        final Long price = shout.getPrice();
        if (price == null) {
            return null;
        } else {
            return PriceUtils.formatPriceWithCurrency(shout.getPrice(), context.getResources(), shout.getCurrency());
        }
    }

    @NonNull
    public Optional<Integer> getCountryResId() {
        return ResourcesHelper.getCountryResId(context, shout.getLocation());
    }

    public void onShoutSelected() {
        shoutSelectedObserver.onNext(shout.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ShoutAdapterItem that = (ShoutAdapterItem) o;

        return shout.equals(that.shout);

    }

    @Override
    public int hashCode() {
        return shout.hashCode();
    }
}
