package com.shoutit.app.android.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import com.shoutit.app.android.view.main.DeepLinksHelper;


public class UpNavigationHelper {

    private final Activity activity;

    public UpNavigationHelper(Activity activity) {
        this.activity = activity;
    }

    public void onUpButtonClicked() {
        if (DeepLinksHelper.isFromDeeplink(activity.getIntent()) && activity.isTaskRoot()) {
            final Intent upIntent = NavUtils.getParentActivityIntent(activity);
            TaskStackBuilder.create(activity)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            handleActivityFinish();
        }
    }

    public void handleActivityFinish() {
        activity.finish();
    }
}
