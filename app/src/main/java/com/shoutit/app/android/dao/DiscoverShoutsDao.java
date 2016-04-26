package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ShoutsResponse;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;

public class DiscoverShoutsDao {

    private final static int PAGE_SIZE = 20;

    private final LoadingCache<String, ShoutsDao> shoutsCache;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final ApiService apiService;

    public DiscoverShoutsDao(@Nonnull @NetworkScheduler Scheduler networkScheduler,
                             @Nonnull ApiService apiService) {
        this.networkScheduler = networkScheduler;
        this.apiService = apiService;

        shoutsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ShoutsDao>() {
                    @Override
                    public ShoutsDao load(@Nonnull String discoverId) throws Exception {
                        return new ShoutsDao(discoverId);
                    }
                });
    }

    public class ShoutsDao extends BaseShoutsDao {
        @Nonnull
        private final String discoverId;

        public ShoutsDao(@Nonnull final String discoverId) {
            super(networkScheduler);
            this.discoverId = discoverId;
        }

        @Nonnull
        @Override
        Observable<ShoutsResponse> getShoutsRequest(int pageNumber) {
            return apiService
                    .shoutsForDiscoverItem(discoverId, pageNumber, PAGE_SIZE)
                    .subscribeOn(networkScheduler);
        }
    }

    @Nonnull
    public Observable<ResponseOrError<ShoutsResponse>> getShoutsObservable(@Nonnull String discoverId) {
        return shoutsCache.getUnchecked(discoverId).getShoutsObservable();
    }

    @Nonnull
    public ShoutsDao getShoutsDao(String discoverId) {
        return shoutsCache.getUnchecked(discoverId);
    }

    @Nonnull
    public Observer<Object> getRefreshObserver(@Nonnull String discoverId) {
        return shoutsCache.getUnchecked(discoverId).getRefreshObserver();
    }
}
