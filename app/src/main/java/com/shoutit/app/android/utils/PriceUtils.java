package com.shoutit.app.android.utils;

import java.text.DecimalFormat;

public class PriceUtils {

    public static String formatPrice(long price) {
        final float formattedPrice = price / 100;
        return new DecimalFormat("#.##").format(formattedPrice);
    }
}
