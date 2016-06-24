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
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class ListenersDaos {

    private static final Integer PAGE_SIZE = 20;

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
        private final Observable<ResponseOrError<ProfilesListResponse>> listeningObservable;
        @Nonnull
        private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private PublishSubject<ResponseOrError<ProfilesListResponse>> updateResponseLocally = PublishSubject.create();

        public ListenersDao(final ApiService apiService,
                            @NetworkScheduler final Scheduler networkScheduler,
                            @Nonnull final String userName) {

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

                                final Observable<ProfilesListResponse> apiRequest = apiService
                                        .listeners(userName, pageNumber, PAGE_SIZE)
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

            listeningObservable = loadMoreSubject.startWith((Object) null)
                    .lift(loadMoreOperator)
                    .compose(MoreOperators.<ProfilesListResponse>refresh(refreshSubject))
                    .compose(ResponseOrError.<ProfilesListResponse>toResponseOrErrorObservable())
                    .mergeWith(updateResponseLocally)
                    .mergeWith(Observable.<ResponseOrError<ProfilesListResponse>>never());

        }

        @NonNull
        public Observable<ResponseOrError<ProfilesListResponse>> getLstenersObservable() {
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

        @Nonnull
        public Observer<ResponseOrError<ProfilesListResponse>> getUpdateResponseLocally() {
            return updateResponseLocally;
        }
    }
}
