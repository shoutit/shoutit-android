package com.shoutit.app.android.utils;

import android.content.Context;
import android.os.Build;
import android.view.View;

import com.google.common.base.Strings;

import java.util.Locale;

public class RtlUtils {

    public static boolean isRtlEnable(Context context) {
        if (Build.VERSION.SDK_INT >= 17) {
            return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        } else {
            return isRTL(context);
        }
    }

    public static boolean isRTL(Context context) {
        return isRTL(Locale.getDefault(), context);
    }

    public static boolean isRTL(Locale locale, Context context) {
        final String displayName = locale.getDisplayName();

        if (displayName != null) {
            final int directionality = Character.getDirectionality(displayName.charAt(0));
            return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                    directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
        } else {
            final String language = context.getResources().getConfiguration().locale.getLanguage();
            return "iw".equals(language) || "ar".equals(language) || "he".equals(language);
        }
    }
}
