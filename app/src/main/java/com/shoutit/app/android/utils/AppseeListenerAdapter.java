package com.shoutit.app.android.utils;

import com.appsee.AppseeListener;
import com.appsee.AppseeScreenDetectedInfo;
import com.appsee.AppseeSessionEndedInfo;
import com.appsee.AppseeSessionEndingInfo;
import com.appsee.AppseeSessionStartedInfo;
import com.appsee.AppseeSessionStartingInfo;

public abstract class AppseeListenerAdapter implements AppseeListener {

    @Override
    public void onAppseeSessionStarting(AppseeSessionStartingInfo appseeSessionStartingInfo) {

    }

    @Override
    public void onAppseeSessionStarted(AppseeSessionStartedInfo appseeSessionStartedInfo) {

    }

    @Override
    public void onAppseeSessionEnding(AppseeSessionEndingInfo appseeSessionEndingInfo) {

    }

    @Override
    public void onAppseeSessionEnded(AppseeSessionEndedInfo appseeSessionEndedInfo) {

    }

    @Override
    public void onAppseeScreenDetected(AppseeScreenDetectedInfo appseeScreenDetectedInfo) {

    }
}
