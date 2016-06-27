package com.shoutit.app.android.api;

import android.os.Build;
import android.text.TextUtils;

import com.appunite.rx.dagger.NetworkScheduler;
import com.google.common.base.Optional;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.UserPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final UserPreferences userPreferences;

    public AuthInterceptor(@NetworkScheduler UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request original = chain.request();

        final String token = userPreferences.getAuthToken().orNull();
        if (TextUtils.isEmpty(token)) {
            return chain.proceed(original);
        } else {
            final Request.Builder authorizationBuilder = original.newBuilder()
                    .header(Headers.AUTHORIZATION, Headers.TOKEN_PREFIX + token)
                    .header(Headers.USER_AGENT, getUserAgent());
            final Optional<String> pageId = userPreferences.getPageId();
            if (pageId.isPresent()) {
                authorizationBuilder.addHeader(Headers.AUTHORIZATION_PAGE_ID, pageId.get());
            }

            return chain.proceed(authorizationBuilder.build());
        }
    }

    private String getUserAgent() {
        return "Shoutit " + BuildConfig.BUILD_TYPE + "/"
                + BuildConfig.APPLICATION_ID + "(" + BuildConfig.buildNumber + "; " +
                "OS Version Android " + Build.VERSION.RELEASE + " (Build " + BuildConfig.commitId + "))";
    }
}
