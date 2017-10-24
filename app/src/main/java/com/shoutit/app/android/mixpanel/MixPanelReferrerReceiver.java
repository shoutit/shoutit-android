package com.shoutit.app.android.mixpanel;


import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shoutit.app.android.App;

public class MixPanelReferrerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent((Application) context).mixPanel().utmParamsFromUri(intent.getData());
    }
}
