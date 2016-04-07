package com.appunite.appunitegcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

public class AppuniteGcmListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        final String message = data.getString("message");
        LoggingHelper.log("GcmPresenter", "Message: " + message);

        AppuniteGcm.getInstance()
                .getBundlePublishObserver()
                .onNext(data);
    }
}