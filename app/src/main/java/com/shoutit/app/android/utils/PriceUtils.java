package com.shoutit.app.android.utils;

import java.text.DecimalFormat;

public class PriceUtils {

    public static String formatPrice(long price) {
        final float formattedPrice = (float) price / 100;
        return new DecimalFormat("#.##").format(formattedPrice);
    }

    public static long getPriceInCents(String price) {
        final double doublePrice = Double.parseDouble(price);
        final double centsPrice = doublePrice * 100;
        return (long) centsPrice;
    }
}
