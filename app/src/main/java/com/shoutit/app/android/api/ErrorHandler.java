package com.shoutit.app.android.api;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.errors.ApiErrors;
import com.shoutit.app.android.utils.ApiErrorThrowable;
import com.shoutit.app.android.utils.LogHelper;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public class ErrorHandler {

    private static final String TAG = ErrorHandler.class.getSimpleName();

    @Nonnull
    public static String getErrorMessage(Throwable throwable, Context context) {
        if (throwable != null && throwable instanceof IOException) {
            return context.getString(R.string.error_connection);
        }

        if (throwable == null || !(throwable instanceof HttpException)) {
            return defaultMessage(context);
        }

        final Response<?> response = ((HttpException) throwable).response();
        final ResponseBody errorBody = response.errorBody();
        if (errorBody == null) {
            return defaultMessage(context);
        }

        final Gson gson = new Gson();
        final ApiErrors apiErrors;
        try {
            apiErrors = gson.fromJson(errorBody.charStream(), ApiErrors.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            LogHelper.logThrowableAndCrashlytics(TAG, "Cannot parse error", e);
            return defaultMessage(context);
        }

        if (apiErrors == null || apiErrors.getError() == null) {
            return defaultMessage(context);
        }

        LogHelper.logThrowableAndCrashlytics(TAG, "Api Error", new ApiErrorThrowable(apiErrors.getError()));
        if (shouldGetErrorFromErrorsList(apiErrors.getError().getCode())) {
            return apiErrors.getError().getErrors().get(0).getMessage();
        } else {
            return apiErrors.getError().getMessage();
        }
    }

    private static boolean shouldGetErrorFromErrorsList(int httpCode) {
        return httpCode == HttpURLConnection.HTTP_BAD_REQUEST;
    }

    @Nonnull
    private static String defaultMessage(Context context) {
        return context.getString(R.string.error_default);
    }
}
