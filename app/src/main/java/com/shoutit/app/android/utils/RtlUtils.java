package com.shoutit.app.android.utils;

import android.content.Context;
import android.view.View;

public class RtlUtils {

    public static boolean isRtlEnable(Context context) {
        return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }
}
