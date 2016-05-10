package com.shoutit.app.android.utils;

import android.content.Context;

import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    @Nullable
    public static String getShoutTypeText(@Nonnull Context context, @Nullable String shoutType) {
        if (shoutType == null) {
            return null;
        } else if (Shout.TYPE_OFFER.equalsIgnoreCase(shoutType)) {
            return context.getString(R.string.shout_type_offer);
        } else if (Shout.TYPE_REQUEST.equalsIgnoreCase(shoutType)) {
            return context.getString(R.string.shout_type_request);
        } else {
            return null;
        }
    }
}
