package com.shoutit.app.android.utils;

import java.text.DecimalFormat;

public class PriceUtils {

    public static String formatPrice(float price) {
        return new DecimalFormat("#.##").format(price);
    }
}
