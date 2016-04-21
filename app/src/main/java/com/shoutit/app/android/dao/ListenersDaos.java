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

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ListenersDaos {

    private final LoadingCache<String, ListenersDao> daoCache;

    public ListenersDaos(@Nonnull final ApiService apiService,
                         @Nonnull @NetworkScheduler final Scheduler networkScheduler) {

        daoCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ListenersDao>() {
                    @Override
                    public ListenersDao load(@Nonnull String userName) throws Exception {
                        return new ListenersDao(apiService, networkScheduler, userName);
                    }
                });
    }

    public ListenersDao getDao(@Nonnull String userName) {
        return daoCache.getUnchecked(userName);
    }

    public class ListenersDao {

        @Nonnull
        private final Observable<ResponseOrError<ListeningResponse>> listeningObservable;
        @Nonnull
        private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();

        public ListenersDao(final ApiService apiService,
                            @NetworkScheduler final Scheduler networkScheduler,
                            @Nonnull final String userName) {

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

                                final Observable<ListeningResponse> apiRequest = apiService
                                        .listeners(userName)
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
                    .compose(MoreOperators.<ResponseOrError<ListeningResponse>>refresh(refreshSubject))
                    .compose(MoreOperators.<ResponseOrError<ListeningResponse>>cacheWithTimeout(networkScheduler));
        }

        @NonNull
        public Observable<ResponseOrError<ListeningResponse>> getLstenersObservable() {
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
