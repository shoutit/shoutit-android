package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.model.MergeListeningResponses;
import com.shoutit.app.android.view.listenings.ListeningsPresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ListeningsDao {

    private static final int PAGE_SIZE = 20;

    private final ApiService apiService;
    private final Scheduler networkScheduler;
    private final LoadingCache<ListeningsPresenter.ListeningsType, ListeningDao> daosCache;

    public ListeningsDao(final ApiService apiService,
                         @NetworkScheduler final Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        daosCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<ListeningsPresenter.ListeningsType, ListeningDao>() {
                    @Override
                    public ListeningDao load(@Nonnull ListeningsPresenter.ListeningsType key) throws Exception {
                        return new ListeningDao(key);
                    }
                });
    }

    @Nonnull
    public ListeningDao getDao(ListeningsPresenter.ListeningsType listeningsType) {
        return daosCache.getUnchecked(listeningsType);
    }

    public class ListeningDao {

        @Nonnull
        private final Observable<ResponseOrError<ListeningResponse>> listeningObservable;
        @Nonnull
        private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();

        private final ListeningsPresenter.ListeningsType listeningsType;

        public ListeningDao(ListeningsPresenter.ListeningsType listeningsType) {
            this.listeningsType = listeningsType;

            final OperatorMergeNextToken<ListeningResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<ListeningResponse, Observable<ListeningResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<ListeningResponse> call(ListeningResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                if (previousResponse == null) {
                                    pageNumber = 0;
                                }
                                ++pageNumber;

                                final Observable<ListeningResponse> apiRequest = getRequest(pageNumber)
                                        .subscribeOn(networkScheduler);

                                if (previousResponse == null) {
                                    return apiRequest;
                                } else {
                                    return Observable.just(previousResponse).zipWith(apiRequest, new MergeListeningResponses());
                                }
                            } else {
                                return Observable.never();
                            }
                        }
                    });

            listeningObservable = loadMoreSubject.startWith((Object) null)
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<ListeningResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<ListeningResponse>>refresh(refreshSubject));
        }

        @Nonnull
        private Observable<ListeningResponse> getRequest(int page) {
            switch (ListeningsPresenter.ListeningsType.values()[listeningsType.ordinal()]) {
                case USERS:
                    return apiService.usersListenings(page, PAGE_SIZE);
                case PAGES:
                    return apiService.pagesListenings(page, PAGE_SIZE);
                case TAGS:
                    return apiService.tagsListenings(page, PAGE_SIZE);
                default:
                    throw new RuntimeException("Unknown listening type");
            }
        }

        @NonNull
        public Observable<ResponseOrError<ListeningResponse>> getListeningObservable() {
            return listeningObservable;
        }

        @NonNull
        public Observer<Object> getLoadMoreObserver() {
            return loadMoreSubject;
        }

        @Nonnull
        public Observer<Object> getRefreshSubject() {
            return refreshSubject;
        }
    }


}
