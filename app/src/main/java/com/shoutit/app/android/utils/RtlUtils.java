package com.shoutit.app.android.utils;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.TextView;


import java.util.Locale;

import javax.annotation.Nonnull;

public class RtlUtils {

    public static boolean isRtlEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
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

    public static void setTextDirection(@Nonnull Context context, @Nonnull TextView textView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextDirection(isRtlEnabled(context) ? View.TEXT_DIRECTION_RTL : View.TEXT_DIRECTION_LTR);
        }
    }

    public static void setLayoutDirection(View view, int layoutDirection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setLayoutDirection(layoutDirection);
        }
    }
}
