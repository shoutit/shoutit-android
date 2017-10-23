package com.shoutit.app.android.api;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.appunite.rx.dagger.NetworkScheduler;
import com.google.common.base.Optional;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.RefreshTokenRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.LanguageHelper;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.TokenUtils;
import com.shoutit.app.android.view.intro.IntroActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

public class AuthInterceptor implements Interceptor {

    private static final String TAG = AuthInterceptor.class.getSimpleName();

    private final UserPreferences userPreferences;
    private final Context context;
    private final RefreshTokenApiService refreshTokenApiService;
    private final Object lockObject = new Object();

    public AuthInterceptor(@NetworkScheduler UserPreferences userPreferences,
                           @ForApplication Context context,
                           RefreshTokenApiService refreshTokenApiService) {
        this.userPreferences = userPreferences;
        this.context = context;
        this.refreshTokenApiService = refreshTokenApiService;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request original = chain.request();

        final Request.Builder authorizationBuilder = original.newBuilder()
                .header(Headers.ACCEPT_LANGUAGE, LanguageHelper.getAcceptLanguage());

        final String token = userPreferences.getAuthToken().orNull();
        if (TextUtils.isEmpty(token)) {
            return chain.proceed(authorizationBuilder.build());
        } else {
            final boolean isTokenExpired = TokenUtils.isTokenExpired(userPreferences);

            if (isTokenExpired) {
                synchronized (lockObject) {
                    final boolean tokenExpired = TokenUtils.isTokenExpired(userPreferences);

                    if (tokenExpired) {
                        LogHelper.logIfDebug(TAG, "Token expired");
                        final retrofit2.Response<SignResponse> refreshTokenResponse = refreshToken(token);

                        if (refreshTokenResponse.isSuccessful()) {
                            final SignResponse signResponse = refreshTokenResponse.body();
                            userPreferences.setLoggedIn(signResponse.getAccessToken(), signResponse.getExpiresIn(),
                                    signResponse.getRefreshToken(), signResponse.getProfile());
                            LogHelper.logIfDebug(TAG, "Token refreshed");
                        } else {
                            LogHelper.logThrowableAndCrashlytics(TAG, "Token refresh failed. Logging out user.",
                                    new Throwable("Failed to refresh token with error response: " + refreshTokenResponse.errorBody().string()));
                            userPreferences.logout();
                            context.startActivity(IntroActivity.newIntent(context)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra(IntroActivity.EXTRA_REFRESH_TOKEN_FAILED, true));

                            return ignoredRequest(chain);
                        }
                    }
                }
            }

            authorizationBuilder.addHeader(Headers.AUTHORIZATION, Headers.TOKEN_PREFIX + userPreferences.getAuthToken().orNull());
            authorizationBuilder.addHeader(Headers.USER_AGENT, getUserAgent());
            final Optional<String> pageId = userPreferences.getPageId();
            if (pageId.isPresent()) {
                authorizationBuilder.addHeader(Headers.AUTHORIZATION_PAGE_ID, pageId.get());
            }

            return chain.proceed(authorizationBuilder.build());
        }
    }

    private Response ignoredRequest(Chain chain) {
        return new Response.Builder()
                .code(600)
                .protocol(Protocol.HTTP_1_1)
                .request(chain.request())
                .build();
    }

    private retrofit2.Response<SignResponse> refreshToken(@NonNull String currentToken) throws IOException {
        final String refreshToken = userPreferences.getRefreshToken();
        final Call<SignResponse> signResponseCall = refreshTokenApiService.refreshToken(
                Headers.TOKEN_PREFIX + currentToken, new RefreshTokenRequest(refreshToken));
        return signResponseCall.execute();
    }

    private String getUserAgent() {
        return "Shoutit " + BuildConfig.BUILD_TYPE + "/"
                + BuildConfig.APPLICATION_ID + "(" + BuildConfig.buildNumber + "; " +
                "OS Version Android " + Build.VERSION.RELEASE + " (Build " + BuildConfig.commitId + "))";
    }
}
