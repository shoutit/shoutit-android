package com.shoutit.app.android.dao;


import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.constants.RequestsConstants;
import com.shoutit.app.android.model.LocationPointer;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ShoutsDao {

    private final static int PAGE_SIZE = 20;

    @Nonnull
    private final PublishSubject<Object> loadMoreHomeShoutsSubject = PublishSubject.create();
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final LoadingCache<LocationPointer, HomeShoutsDao> homeCache;

    public ShoutsDao(@Nonnull final ApiService apiService,
                     @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                     @Nonnull final UserPreferences userPreferences) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;
        this.userPreferences = userPreferences;

        homeCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<LocationPointer, HomeShoutsDao>() {
                    @Override
                    public HomeShoutsDao load(@Nonnull LocationPointer locationPointer) throws Exception {
                        return new HomeShoutsDao(locationPointer);
                    }
                });
    }

    @Nonnull
    public Observable<ResponseOrError<ShoutsResponse>> getHomeShoutsObservable(@Nonnull LocationPointer locationPointer) {
        return homeCache.getUnchecked(locationPointer).getShoutsObservable();
    }

    @Nonnull
    public Observer<Object> getLoadMoreHomeShoutsObserver() {
        return loadMoreHomeShoutsSubject;
    }

    public class HomeShoutsDao {

        @Nonnull
        private final Observable<ResponseOrError<ShoutsResponse>> homeShoutsObservable;

        public HomeShoutsDao(@Nonnull final LocationPointer locationPointer) {

            final OperatorMergeNextToken<ShoutsResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<ShoutsResponse, Observable<ShoutsResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<ShoutsResponse> call(ShoutsResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                ++pageNumber;
                                final Observable<ShoutsResponse> apiRequest;
                                if (userPreferences.isNormalUser()) {
                                    apiRequest = apiService
                                            .home(RequestsConstants.USER_ME, pageNumber, PAGE_SIZE)
                                            .subscribeOn(networkScheduler);
                                } else {
                                    apiRequest = apiService
                                            .shoutsForCity(locationPointer.getCountryCode(),
                                                    locationPointer.getCity(), pageNumber, PAGE_SIZE)
                                            .subscribeOn(networkScheduler);
                                }

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

            homeShoutsObservable = loadMoreHomeShoutsSubject.startWith((Object) null)
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<ShoutsResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ShoutsResponse>repeatOnError(networkScheduler))
                    .compose(MoreOperators.<ResponseOrError<ShoutsResponse>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<ShoutsResponse>> getShoutsObservable() {
            return homeShoutsObservable;
        }
    }
}
