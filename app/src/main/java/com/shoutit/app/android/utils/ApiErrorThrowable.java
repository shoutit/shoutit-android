package com.shoutit.app.android.utils;


import android.annotation.SuppressLint;

import com.shoutit.app.android.api.errors.ApiErrors;

public class ApiErrorThrowable extends Throwable {

    @SuppressLint("DefaultLocale")
    public ApiErrorThrowable(ApiErrors.Error error) {
        super(String.format("response : \n error code : %d\n mainMessage: %s \n subMessages: %s \n developerMessage: %s",
                error.getCode(), error.getMessage(), error.getContcatanatedMessages(), error.getDeveloperMessage()));
    }
}
