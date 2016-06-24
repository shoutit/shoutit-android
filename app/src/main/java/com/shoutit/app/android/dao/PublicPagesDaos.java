package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ProfilesListResponse;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class PublicPagesDaos {
    private static final int PAGE_SIZE = 20;

    private final LoadingCache<String, PublicPagesDao> daoCache;
    private final ApiService apiService;
    private final Scheduler networkScheduler;

    public PublicPagesDaos(ApiService apiService,
                           @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        daoCache = CacheBuilder.newBuilder().build(
                new CacheLoader<String, PublicPagesDao>() {
                    @Override
                    public PublicPagesDao load(@Nonnull String countryCode) throws Exception {
                        return new PublicPagesDao(countryCode);
                    }
                }
        );
    }

    public PublicPagesDao getDao(@Nonnull String countryCode) {
        return daoCache.getUnchecked(countryCode);
    }

    public class PublicPagesDao {

        private final Observable<ResponseOrError<ProfilesListResponse>> pagesObservable;
        private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();
        private final PublishSubject<ResponseOrError<ProfilesListResponse>> updatedProfilesLocallySubject = PublishSubject.create();

        public PublicPagesDao(@Nonnull String countryCode) {

            final OperatorMergeNextToken<ProfilesListResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<ProfilesListResponse, Observable<ProfilesListResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<ProfilesListResponse> call(ProfilesListResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                if (previousResponse == null) {
                                    pageNumber = 0;
                                }
                                ++pageNumber;

                                final Observable<ProfilesListResponse> apiRequest = apiService.getPublicPages(
                                        countryCode, pageNumber, PAGE_SIZE)
                                        .subscribeOn(networkScheduler);

                                if (previousResponse == null) {
                                    return apiRequest;
                                } else {
                                    return Observable.just(previousResponse).zipWith(apiRequest, new MergeProfilesListResponses());
                                }
                            } else {
                                return Observable.never();
                            }
                        }
                    });


            pagesObservable = loadMoreSubject.startWith((Object) null)
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<ProfilesListResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<ProfilesListResponse>>refresh(refreshSubject))
                    .mergeWith(updatedProfilesLocallySubject)
                    .compose(MoreOperators.<ResponseOrError<ProfilesListResponse>>cacheWithTimeout(networkScheduler));

        }

        public Observable<ResponseOrError<ProfilesListResponse>> getPagesObservable() {
            return pagesObservable;
        }

        public Observer<Object> getLoadMoreSubject() {
            return loadMoreSubject;
        }

        @Nonnull
        public PublishSubject<Object> getRefreshSubject() {
            return refreshSubject;
        }

        @Nonnull
        public Observer<ResponseOrError<ProfilesListResponse>> updatedProfileLocallyObserver() {
            return updatedProfilesLocallySubject;
        }
    }
}
