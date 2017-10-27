package com.shoutit.app.android.api;

import com.shoutit.app.android.api.model.RefreshTokenRequest;
import com.shoutit.app.android.api.model.SignResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RefreshTokenApiService {

    @POST("oauth2/access_token")
    Call<SignResponse> refreshToken(@Header(Headers.AUTHORIZATION) String authKey, @Body RefreshTokenRequest body);
}
