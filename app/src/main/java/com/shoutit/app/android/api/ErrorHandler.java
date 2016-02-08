package com.shoutit.app.android.api;

import android.support.annotation.StringRes;

import com.shoutit.app.android.R;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit2.HttpException;
import retrofit2.Response;

public class ErrorHandler {

    private static final String REQUEST_RESET_PASSWORD = "reset_password";
    private static final String REQUEST_RESET_LOGIN = "access_token";

    @StringRes
    public static int getErrorMessageId(Throwable throwable) {
        if (throwable != null && throwable instanceof IOException) {
            return R.string.error_connection;
        }

        if (throwable == null || !(throwable instanceof HttpException)) {
            return  defaultMessage();
        }

        final int httpCode = ((HttpException) throwable).response().code();
        final Response<?> response = ((HttpException) throwable).response();
        String requestUrl = "";
        if (response != null) {
            requestUrl = requestUrl + response.raw().request().url().url().toString();
        }

        if (httpCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            if (requestUrl.contains(REQUEST_RESET_PASSWORD)) {
                return R.string.error_email_not_found;
            } else if (requestUrl.contains(REQUEST_RESET_LOGIN)) {
                return R.string.error_wrong_email_or_password;
            }
        }

        return defaultMessage();
    }

    @StringRes
    private static int defaultMessage() {
        return R.string.error_default;
    }
}
