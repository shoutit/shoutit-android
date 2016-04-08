package com.shoutit.app.android.api.model;

public class RegisterDeviceRequest {

    private final PushTokens pushTokens;

    public RegisterDeviceRequest(String token) {
        this.pushTokens = new PushTokens(token);
    }

    private class PushTokens {
        private final String gcm;

        private PushTokens(String gcm) {
            this.gcm = gcm;
        }
    }
}
