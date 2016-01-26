package com.shoutit.app.android.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;

public class SystemUIUtils {

    public static void setFullscreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

}
