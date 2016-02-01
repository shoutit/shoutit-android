package com.shoutit.app.android.api;

import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.ShoutsResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface ApiService {

    @GET("discover")
    Observable<DiscoverResponse> discovers(@Query("country") String country,
                                           @Query("page") int page,
                                           @Query("page_size") int pageSize);

    @GET("users/{user_name}/home")
    Observable<ShoutsResponse> home(@Path("user_name") String userName,
                                    @Query("page") int page,
                                    @Query("page_size") int pageSize);

    @GET("shouts")
    Observable<ShoutsResponse> shoutsForCountry(@Query("city") String city,
                                                @Query("page") int page,
                                                @Query("page_size") int pageSize);
    @POST("oauth2/access_token")
    Observable<SignResponse> login(@Body EmailLoginRequest request);

    @POST("oauth2/access_token")
    Observable<SignResponse> signup(@Body EmailSignupRequest request);
}
