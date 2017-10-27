package com.shoutit.app.android.utils;

import android.content.Context;
import android.view.View;

import com.appsee.Appsee;
import com.shoutit.app.android.R;

public class AppseeHelper {

    public static void start(Context context) {
        Appsee.start(context.getString(R.string.app_see_api_key));
    }

    public static void markViewAsSensitive(View view) {
        Appsee.markViewAsSensitive(view);
    }

    public static void setUserId(String userId) {
        Appsee.setUserId(userId);
    }
}
