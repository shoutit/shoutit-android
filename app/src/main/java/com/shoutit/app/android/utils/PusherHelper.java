package com.shoutit.app.android.utils;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.util.HttpAuthorizer;
import com.shoutit.app.android.BuildConfig;

public class PusherHelper {

    private Pusher mPusher;

    public void init(@NonNull String token) {
        final HttpAuthorizer authorizer = new HttpAuthorizer(BuildConfig.API_URL + "pusher/auth");
        authorizer.setHeaders(ImmutableMap.of("Authorization", "Bearer " + token));
        final PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        mPusher = new Pusher(BuildConfig.DEBUG ? "7bee1e468fabb6287fc5" : "86d676926d4afda44089", options);
    }

    public Pusher getPusher() {
        Preconditions.checkState(mPusher != null);
        return mPusher;
    }
}
