package com.shoutit.app.android.utils;

import android.os.Build;
import android.support.annotation.DrawableRes;
import android.widget.TextView;

import javax.annotation.Nonnull;

public class ImageHelper {

    public static void setStartCompoundRelativeDrawable(@Nonnull TextView textView, @DrawableRes int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableId, 0, 0, 0);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
        }
    }
}
