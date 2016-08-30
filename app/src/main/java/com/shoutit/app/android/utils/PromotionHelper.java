package com.shoutit.app.android.utils;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shoutit.app.android.adapteritems.BaseShoutAdapterItem;
import com.shoutit.app.android.api.model.Label;
import com.shoutit.app.android.api.model.Promotion;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

public class PromotionHelper {

    private static boolean hasPromotion(Shout shout) {
        return shout.getPromotion() != null && !shout.getPromotion().isExpired();
    }

    private static ShoutAdapterItem.PromotionInfo promotionInfo(Promotion promotion) {
        final Label label = promotion.getLabel();
        return new ShoutAdapterItem.PromotionInfo(Color.parseColor(label.getBgColor()), Color.parseColor(label.getColor()), label.getName());
    }

    @Nullable
    public static ShoutAdapterItem.PromotionInfo promotionInfoOrNull(@NonNull Shout shout) {
        final boolean hasPromotion = hasPromotion(shout);
        if (hasPromotion) {
            return promotionInfo(shout.getPromotion());
        } else {
            return null;
        }
    }

    @Nullable
    public static BaseShoutAdapterItem.PromotionInfo promotionsInfoOrNull(@NonNull Shout shout) {
        final boolean hasPromotion = hasPromotions(shout);
        if (hasPromotion) {
            return promotionInfos(shout.getPromotion());
        } else {
            return null;
        }
    }

    private static boolean hasPromotions(Shout shout) {
        return shout.getPromotion() != null && !shout.getPromotion().isExpired();
    }

    private static BaseShoutAdapterItem.PromotionInfo promotionInfos(Promotion promotion) {
        final Label label = promotion.getLabel();
        return new BaseShoutAdapterItem.PromotionInfo(Color.parseColor(label.getBgColor()), Color.parseColor(label.getColor()), label.getName());
    }
}
