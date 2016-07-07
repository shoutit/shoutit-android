package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class ApiMessageResponse {

    @NonNull
    private final String success;

    public ApiMessageResponse(@NonNull String success) {
        this.success = success;
    }

    @NonNull
    public String getSuccess() {
        return success;
    }
}
