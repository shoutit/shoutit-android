package com.shoutit.app.android.api.model;

import javax.annotation.Nullable;

public class RegisterDeviceRequest {

    public static final String EMPTY_PUSHTOKEN = "{" +
            "\"push_tokens\" : {" +
            "\"gcm\" : null" +
            "}" +
            "}";

    private final PushTokens pushTokens;

    public RegisterDeviceRequest(@Nullable String token) {
        this.pushTokens = new PushTokens(token);
    }

    private class PushTokens {
        private final String gcm;

        private PushTokens(@Nullable String gcm) {
            this.gcm = gcm;
        }
    }
}
