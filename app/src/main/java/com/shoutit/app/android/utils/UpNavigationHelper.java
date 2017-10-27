package com.shoutit.app.android.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import com.shoutit.app.android.view.main.DeepLinksHelper;
import com.shoutit.app.android.view.main.MainActivity;


public class UpNavigationHelper {

    public static final String IN_APP_DEEP_LINK = "in_app_deep_link";

    private final Activity activity;

    public UpNavigationHelper(Activity activity) {
        this.activity = activity;
    }

    public void onUpButtonClicked() {
        final boolean isInAppDeepLink = activity.getIntent().getBooleanExtra(IN_APP_DEEP_LINK, false);

        if (DeepLinksHelper.isFromDeeplink(activity.getIntent()) && !isInAppDeepLink) {
            Intent upIntent = NavUtils.getParentActivityIntent(activity);
            upIntent = upIntent == null ? MainActivity.newIntent(activity) : upIntent;

            TaskStackBuilder.create(activity)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();

            activity.finish();
        } else {
            handleActivityFinish();
        }
    }

    public void handleActivityFinish() {
        activity.finish();
    }
}
