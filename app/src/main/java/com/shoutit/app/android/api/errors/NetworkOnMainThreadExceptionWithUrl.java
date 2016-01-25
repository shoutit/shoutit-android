package com.shoutit.app.android.api.errors;

public class NetworkOnMainThreadExceptionWithUrl extends RuntimeException {

    public NetworkOnMainThreadExceptionWithUrl(String message) {
        super(message);
    }
}
