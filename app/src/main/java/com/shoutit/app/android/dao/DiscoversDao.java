package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import rx.Observable;
import rx.Scheduler;

@Singleton
public class DiscoversDao {

    private final LoadingCache<String, DiscoverItemDao> cache;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;
    private final Observable<ResponseOrError<DiscoverResponse>> homeDiscoverObservable;

    public DiscoversDao(@Nonnull final ApiService apiService,
                        @Nonnull UserPreferences userPreferences,
                        @Nonnull @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        homeDiscoverObservable = apiService
                .discovers(userPreferences.getUserCountryCode(), null, null)
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<DiscoverResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<DiscoverResponse>>cacheWithTimeout(networkScheduler));

        cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, DiscoverItemDao>() {
                    @Override
                    public DiscoverItemDao load(@Nonnull final String id) throws Exception {
                        return new DiscoverItemDao(id);
                    }
                });
    }

    public class DiscoverItemDao {

        private final Observable<ResponseOrError<DiscoverItemDetailsResponse>> discoverItemObservable;

        public DiscoverItemDao(@Nonnull String id) {
            discoverItemObservable = apiService.discoverItem(id)
                    .subscribeOn(networkScheduler)
                    .compose(ResponseOrError.<DiscoverItemDetailsResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<DiscoverItemDetailsResponse>>cacheWithTimeout(networkScheduler));
        }

        public Observable<ResponseOrError<DiscoverItemDetailsResponse>> getDiscoverItemObservable() {
            return discoverItemObservable;
        }
    }

    @Nonnull
    public DiscoverItemDao discoverItemDao(@Nonnull final String id) {
        return cache.getUnchecked(id);
    }

    public Observable<ResponseOrError<DiscoverResponse>> getHomeDiscoverObservable() {
        return homeDiscoverObservable;
    }
}
