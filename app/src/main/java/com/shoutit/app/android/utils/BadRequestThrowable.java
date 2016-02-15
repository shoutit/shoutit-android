package com.shoutit.app.android.utils;

import okhttp3.Response;

public class BadRequestThrowable extends Throwable {

    public BadRequestThrowable(Response response) {
        super(String.format("endpoint : %s \nresponse : %s", response.request().url(), response.toString()));
    }
}
