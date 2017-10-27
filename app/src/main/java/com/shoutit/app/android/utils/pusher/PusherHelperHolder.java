package com.shoutit.app.android.utils.pusher;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Provider;

public class PusherHelperHolder {

    private final Provider<PusherHelper> mPusherHelperProvider;

    private final AtomicReference<PusherHelper> mPusherHelper;

    @Inject
    public PusherHelperHolder(Provider<PusherHelper> pusherHelperProvider) {
        mPusherHelperProvider = pusherHelperProvider;
        mPusherHelper = new AtomicReference<>();
    }

    public PusherHelper getPusherHelper() {
        return mPusherHelper.get();
    }

    public PusherHelper newInstance() {
        final PusherHelper newPusherHelper = mPusherHelperProvider.get();
        final PusherHelper pusherHelper = mPusherHelper.get();
        mPusherHelper.compareAndSet(pusherHelper, newPusherHelper);
        return mPusherHelper.get();
    }
}
