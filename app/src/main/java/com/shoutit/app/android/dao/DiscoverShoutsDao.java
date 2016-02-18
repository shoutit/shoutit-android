package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ShoutsResponse;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class DiscoverShoutsDao {

    private final static int PAGE_SIZE = 20;

    @Nonnull
    private final PublishSubject<Object> loadMoreShoutsSubject = PublishSubject.create();

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

    public class ShoutsDao {
        @Nonnull
        private final Observable<ResponseOrError<ShoutsResponse>> shoutsObservable;

        public ShoutsDao(@Nonnull final String discoverId) {

            final OperatorMergeNextToken<ShoutsResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<ShoutsResponse, Observable<ShoutsResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<ShoutsResponse> call(ShoutsResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                ++pageNumber;

                                final Observable<ShoutsResponse> apiRequest = apiService
                                        .shoutsForDiscoverItem(discoverId, pageNumber, PAGE_SIZE);
                                if (previousResponse == null) {
                                    return apiRequest;
                                } else {
                                    return Observable.just(previousResponse).zipWith(apiRequest, new MergeShoutsResponses());
                                }
                            } else {
                                return Observable.never();
                            }
                        }
                    });

            shoutsObservable = loadMoreShoutsSubject.startWith((Object) null)
                    .mergeWith(Observable.never())
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<ShoutsResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<ShoutsResponse>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<ShoutsResponse>> getShoutsObservable() {
            return shoutsObservable;
        }
    }

    @Nonnull
    public Observable<ResponseOrError<ShoutsResponse>> getShoutsObservable(@Nonnull String discoverId) {
        return shoutsCache.getUnchecked(discoverId).getShoutsObservable();
    }

    @Nonnull
    public PublishSubject<Object> getLoadMoreShoutsSubject() {
        return loadMoreShoutsSubject;
    }
}
