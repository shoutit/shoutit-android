package com.shoutit.app.android.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.ChangePasswordRequest;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.ResetPasswordRequest;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.TagsRequest;
import com.shoutit.app.android.api.model.UpdateLocationRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;
import com.shoutit.app.android.api.model.login.FacebookLogin;
import com.shoutit.app.android.api.model.login.GoogleLogin;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface ApiService {

    /** Discover **/
    @GET("discover")
    Observable<DiscoverResponse> discovers(@Query("country") String country,
                                           @Query("page") Integer page,
                                           @Query("page_size") Integer pageSize);

    @GET("discover/{id}")
    Observable<DiscoverItemDetailsResponse> discoverItem(@Path("id") String id);



    /** Shouts **/
    @GET("shouts")
    Observable<ShoutsResponse> shoutsForCity(@Query("country") String countryCode,
                                             @Query("city") String city,
                                             @Query("page") Integer page,
                                             @Query("page_size") Integer pageSize);

    @GET("shouts")
    Observable<ShoutsResponse> shoutsForDiscoverItem(@Query("discover") @NonNull String discoverId,
                                                     @Query("page") @Nullable Integer page,
                                                     @Query("page_size") @Nullable Integer pageSize);


    /** OAuth **/
    @POST("oauth2/access_token")
    Observable<SignResponse> login(@Body EmailLoginRequest request);

    @POST("oauth2/access_token")
    Observable<SignResponse> signup(@Body EmailSignupRequest request);

    @POST("oauth2/access_token")
    Observable<SignResponse> facebookLogin(@Body FacebookLogin request);

    @POST("auth/reset_password")
    Observable<ResponseBody> resetPassword(@Body ResetPasswordRequest request);

    @POST("oauth2/access_token")
    Observable<SignResponse> googleLogin(@Body GoogleLogin request);

    @POST("tags/batch_listen")
    Observable<Object> batchListen(@Body TagsRequest request);


    /** User **/
    @GET("users/{user_name}")
    Observable<User> getUser(@Path("user_name") String userName);

    @GET("users/me")
    Observable<User> getMyUser();

    @PATCH("users/me")
    Observable<User> updateUserLocation(@Body UpdateLocationRequest updateLocationRequest);

    @GET("users/{user_name}/home")
    Observable<ShoutsResponse> home(@Path("user_name") String userName,
                                    @Query("page") Integer page,
                                    @Query("page_size") Integer pageSize);


    /** Misc **/
    @GET("misc/geocode")
    Observable<UserLocation> geocode(@Query("latlng") String latlng); // format like latlng=40.722100,-74.046900

    @GET("misc/geocode?latlng=0,0")
    Observable<UserLocation> geocodeDefault();

    @GET("misc/categories")
    Observable<List<Category>> categories();


    /** Auth **/
    @POST("auth/change_password")
    Observable<ResponseBody> changePassword(@Body ChangePasswordRequest changePasswordRequest);

}
