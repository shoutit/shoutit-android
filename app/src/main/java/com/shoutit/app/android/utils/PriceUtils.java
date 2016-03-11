package com.shoutit.app.android.utils;

import android.support.annotation.Nullable;

import java.text.DecimalFormat;

import javax.annotation.Nonnull;

public class PriceUtils {

    @Nonnull
    public static String formatPrice(@Nullable Long price) {
        if (price == null) {
            return "";
        }
        final float formattedPrice = (float) price / 100;
        return new DecimalFormat("#.##").format(formattedPrice);
    }

    public static long getPriceInCents(String price) {
        final double doublePrice = Double.parseDouble(price);
        final double centsPrice = doublePrice * 100;
        return (long) centsPrice;
    }
}
