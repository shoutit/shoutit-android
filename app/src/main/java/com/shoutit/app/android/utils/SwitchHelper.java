package com.shoutit.app.android.utils;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.pusher.PusherHelper;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class SwitchHelper {

    @Nonnull
    private final UserPreferences userPreferences;
    private final PusherHelper mPusherHelper;

    @Inject
    public SwitchHelper(@Nonnull UserPreferences userPreferences,
                        PusherHelper pusherHelper) {
        this.userPreferences = userPreferences;
        mPusherHelper = pusherHelper;
    }

    public void logout() {
        Twilio.unregisterTwillio();
        mPusherHelper.unsubscribeProfileChannel(userPreferences.getUserId());
        mPusherHelper.disconnect();
    }
}