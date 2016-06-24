package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.PagesResponse;

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

        private final Observable<ResponseOrError<PagesResponse>> pagesObservable;
        private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();

        public PublicPagesDao(@Nonnull String countryCode) {

            final OperatorMergeNextToken<PagesResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<PagesResponse, Observable<PagesResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<PagesResponse> call(PagesResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                if (previousResponse == null) {
                                    pageNumber = 0;
                                }
                                ++pageNumber;

                                final Observable<PagesResponse> apiRequest = apiService.getPublicPages(
                                        countryCode, pageNumber, PAGE_SIZE)
                                        .subscribeOn(networkScheduler);

                                if (previousResponse == null) {
                                    return apiRequest;
                                } else {
                                    return Observable.just(previousResponse).zipWith(apiRequest, new MergePagesResponses());
                                }
                            } else {
                                return Observable.never();
                            }
                        }
                    });

            pagesObservable = loadMoreSubject.startWith((Object) null)
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<PagesResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<PagesResponse>>cacheWithTimeout(networkScheduler))
                    .mergeWith(Observable.never());

        }

        public Observable<ResponseOrError<PagesResponse>> getPagesObservable() {
            return pagesObservable;
        }

        public Observer<Object> getLoadMoreSubject() {
            return loadMoreSubject;
        }
    }
}
