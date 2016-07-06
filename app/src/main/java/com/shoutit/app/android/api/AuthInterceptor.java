package com.shoutit.app.android.api;

import android.os.Build;
import android.text.TextUtils;

import com.appunite.rx.dagger.NetworkScheduler;
import com.google.common.base.Optional;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.UserPreferences;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.http.HEAD;

public class AuthInterceptor implements Interceptor {

    private final UserPreferences userPreferences;

    public AuthInterceptor(@NetworkScheduler UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request original = chain.request();

        final Request.Builder authorizationBuilder = original.newBuilder()
                .header(Headers.ACCEPT_LANGUAGE, languageTag(Locale.getDefault()));

        final String token = userPreferences.getAuthToken().orNull();
        if (TextUtils.isEmpty(token)) {
            return chain.proceed(authorizationBuilder.build());
        } else {
            authorizationBuilder.addHeader(Headers.AUTHORIZATION, Headers.TOKEN_PREFIX + token);
            authorizationBuilder.addHeader(Headers.USER_AGENT, getUserAgent());
            final Optional<String> pageId = userPreferences.getPageId();
            if (pageId.isPresent()) {
                authorizationBuilder.addHeader(Headers.AUTHORIZATION_PAGE_ID, pageId.get());
            }

            return chain.proceed(authorizationBuilder.build());
        }
    }

    private String languageTag(Locale locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return locale.toLanguageTag();
        }

        final char SEP = '-';
        String language = locale.getLanguage();
        String region = locale.getCountry();
        String variant = locale.getVariant();

        if (language.equals("no") && region.equals("NO") && variant.equals("NY")) {
            language = "nn";
            region = "NO";
            variant = "";
        }

        if (language.isEmpty() || !language.matches("\\p{Alpha}{2,8}")) {
            language = "und";
        } else if (language.equals("iw")) {
            language = "he";
        } else if (language.equals("in")) {
            language = "id";
        } else if (language.equals("ji")) {
            language = "yi";
        }
        if (!region.matches("\\p{Alpha}{2}|\\p{Digit}{3}")) {
            region = "";
        }
        if (!variant.matches("\\p{Alnum}{5,8}|\\p{Digit}\\p{Alnum}{3}")) {
            variant = "";
        }

        StringBuilder bcp47Tag = new StringBuilder(language);
        if (!region.isEmpty()) {
            bcp47Tag.append(SEP).append(region);
        }
        if (!variant.isEmpty()) {
            bcp47Tag.append(SEP).append(variant);
        }
        return bcp47Tag.toString();
    }

    private String getUserAgent() {
        return "Shoutit " + BuildConfig.BUILD_TYPE + "/"
                + BuildConfig.APPLICATION_ID + "(" + BuildConfig.buildNumber + "; " +
                "OS Version Android " + Build.VERSION.RELEASE + " (Build " + BuildConfig.commitId + "))";
    }
}
