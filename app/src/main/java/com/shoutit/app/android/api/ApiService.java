package com.shoutit.app.android.api;

import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.ShoutsResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface ApiService {

    @GET("discover")
    Observable<DiscoverResponse> discovers(@Query("country") String country,
                                           @Query("page") int page,
                                           @Query("page_size") int pageSize);

    @GET("users/me/home")
    Observable<ShoutsResponse> myHome(@Query("page") int page,
                                      @Query("page_size") int pageSize);

    @GET("shouts")
    Observable<ShoutsResponse> shoutsForCountry(@Query("city") String city);
}
