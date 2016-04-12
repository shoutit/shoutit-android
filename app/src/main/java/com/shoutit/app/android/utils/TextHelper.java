package com.shoutit.app.android.utils;

import java.util.Locale;

public class TextHelper {

    public static String formatListenersNumber(int number) {
        if (number >= 1000) {
            return String.format(Locale.getDefault(), "%1$d%2$s", number / 1000, "K");
        } else {
            return String.valueOf(number);
        }
    }

    public static String formatErrorMessage(String errorMessage){
        return errorMessage.substring(18, errorMessage.length());
    }
}
