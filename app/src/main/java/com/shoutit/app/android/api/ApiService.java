package com.shoutit.app.android.api;

import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.ShoutsResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

public interface ApiService {

    @GET("discover")
    Observable<DiscoverResponse> discovers(@Query("country") String country,
                                           @Query("page") Integer page,
                                           @Query("page_size") Integer pageSize);

    @GET("discover/{id}")
    Observable<DiscoverItemDetailsResponse> discoverItem(@Path("id") String id);

    @GET("users/{user_name}/home")
    Observable<ShoutsResponse> home(@Path("user_name") String userName,
                                    @Query("page") Integer page,
                                    @Query("page_size") Integer pageSize);

    @GET("shouts")
    Observable<ShoutsResponse> shoutsForCountry(@Query("city") String city,
                                                @Query("page") Integer page,
                                                @Query("page_size") Integer pageSize);
    @POST("oauth2/access_token")
    Observable<SignResponse> login(@Body EmailLoginRequest request);

    @POST("oauth2/access_token")
    Observable<SignResponse> signup(@Body EmailSignupRequest request);

    @GET("users/{user_name}")
    Observable<User> getUser(@Path("user_name") String userName);
}
