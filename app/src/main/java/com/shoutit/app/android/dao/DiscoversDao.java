package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.util.LogTransformer;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.model.LocationPointer;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import rx.Observable;
import rx.Scheduler;

public class DiscoversDao {

    @Nonnull
    private final LoadingCache<String, DiscoverItemDao> discoverItemCache;
    @Nonnull
    private final LoadingCache<LocationPointer, DiscoverDao> discoverCache;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;

    public DiscoversDao(@Nonnull final ApiService apiService,
                        @Nonnull @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        discoverCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<LocationPointer, DiscoverDao>() {
                    @Override
                    public DiscoverDao load(@Nonnull final LocationPointer id) throws Exception {
                        return new DiscoverDao(id);
                    }
                });

        discoverItemCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, DiscoverItemDao>() {
                    @Override
                    public DiscoverItemDao load(@NonNull String key) throws Exception {
                        return new DiscoverItemDao(key);
                    }
                });
    }

    @Nonnull
    public Observable<ResponseOrError<DiscoverResponse>> getDiscoverObservable(@Nonnull LocationPointer locationPointer) {
        return discoverCache.getUnchecked(locationPointer).getDiscoverObservable();
    }

    public class DiscoverDao {

        @Nonnull
        private final Observable<ResponseOrError<DiscoverResponse>> discoverObservable;

        public DiscoverDao(@Nonnull LocationPointer locationPointer) {

            discoverObservable = apiService
                    .discovers(locationPointer.getCountryCode(), null, null)
                    .compose(LogTransformer.<DiscoverResponse>transformer("dupa", "dupa1"))
                    .subscribeOn(networkScheduler)
                    .compose(ResponseOrError.<DiscoverResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<DiscoverResponse>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<DiscoverResponse>> getDiscoverObservable() {
            return discoverObservable;
        }
    }

    public class DiscoverItemDao {

        @Nonnull
        private final Observable<ResponseOrError<DiscoverItemDetailsResponse>> discoverItemObservable;

        public DiscoverItemDao(@Nonnull String id) {
            discoverItemObservable = apiService.discoverItem(id)
                    .subscribeOn(networkScheduler)
                    .compose(ResponseOrError.<DiscoverItemDetailsResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<DiscoverItemDetailsResponse>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<DiscoverItemDetailsResponse>> getDiscoverItemObservable() {
            return discoverItemObservable;
        }
    }

    @Nonnull
    public DiscoverItemDao getDiscoverItemDao(@Nonnull final String id) {
        return discoverItemCache.getUnchecked(id);
    }

}
