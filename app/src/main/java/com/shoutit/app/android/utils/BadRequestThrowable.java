package com.shoutit.app.android.utils;

import java.io.IOException;

import okhttp3.Response;

public class BadRequestThrowable extends Throwable {

    public BadRequestThrowable(Response response) throws IOException {
        super(String.format("response : %s\nresponse message : %s", response.toString(), response.body() != null ? response.body().string() : ""));
    }
}
