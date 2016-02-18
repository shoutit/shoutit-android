package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.util.LogTransformer;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.Functions1;
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

import java.util.concurrent.TimeUnit;

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

            final Observable<Object> refreshWithCache = Observable
                    .interval(5, TimeUnit.MINUTES, networkScheduler)
                    .map(Functions1.toObject());

            discoverObservable = apiService
                    .discovers(locationPointer.getCountryCode(), null, null)
                    .subscribeOn(networkScheduler)
                    .compose(MoreOperators.<DiscoverResponse>refresh(refreshWithCache))
                    .compose(ResponseOrError.<DiscoverResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<DiscoverResponse>repeatOnError(networkScheduler))
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

            final Observable<Object> refreshWithCache = Observable
                    .interval(5, TimeUnit.MINUTES, networkScheduler)
                    .map(Functions1.toObject());

            discoverItemObservable = apiService.discoverItem(id)
                    .subscribeOn(networkScheduler)
                    .compose(MoreOperators.<DiscoverItemDetailsResponse>refresh(refreshWithCache))
                    .compose(ResponseOrError.<DiscoverItemDetailsResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<DiscoverItemDetailsResponse>repeatOnError(networkScheduler))
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
