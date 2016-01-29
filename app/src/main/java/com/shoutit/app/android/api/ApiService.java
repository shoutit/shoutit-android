package com.shoutit.app.android.api;

import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface ApiService {

    @POST("oauth2/access_token")
    Observable<SignResponse> login(@Body EmailLoginRequest request);

    @POST("oauth2/access_token")
    Observable<SignResponse> signup(@Body EmailSignupRequest request);
}
