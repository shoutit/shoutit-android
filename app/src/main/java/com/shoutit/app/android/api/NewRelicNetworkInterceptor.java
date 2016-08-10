package com.shoutit.app.android.api;

import com.google.common.primitives.Longs;
import com.newrelic.agent.android.NewRelic;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NewRelicNetworkInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {

        final Request request = chain.request();
        final Response response = chain.proceed(request);

        Long sendTimeInMillis = Longs.tryParse(response.header("OkHttp-Sent-Millis", "0"));
        Long receiveTimeInMillis = Longs.tryParse(response.header("OkHttp-Received-Millis", "0"));
        sendTimeInMillis = sendTimeInMillis == null ? 0 : sendTimeInMillis;
        receiveTimeInMillis = receiveTimeInMillis == null ? 0 : receiveTimeInMillis;

        NewRelic.noticeHttpTransaction(request.url().toString(), request.method(), response.code(),
                sendTimeInMillis, receiveTimeInMillis, 0L, 0L);

        return response;
    }
}
