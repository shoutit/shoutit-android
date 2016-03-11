package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.SuggestionsResponse;
import com.shoutit.app.android.api.model.UserLocation;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class SuggestionsDao {

    private final ApiService apiService;
    private final Scheduler networkScheduler;
    private final LoadingCache<SuggestionsPointer, SuggestionDao> suggestionsCache;

    @Inject
    public SuggestionsDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        suggestionsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<SuggestionsPointer, SuggestionDao>() {
                    @Override
                    public SuggestionDao load(@Nonnull SuggestionsPointer key) throws Exception {
                        return new SuggestionDao(key);
                    }
                });
    }

    @NonNull
    public Observable<ResponseOrError<SuggestionsResponse>> getSuggestionsObservable(SuggestionsPointer suggestionsPointer) {
        return suggestionsCache.getUnchecked(suggestionsPointer).getSuggestionsObservable();
    }

    @Nonnull
    public Observer<ResponseOrError<SuggestionsResponse>> getSuggestionUpdateObserver(SuggestionsPointer suggestionsPointer) {
        return suggestionsCache.getUnchecked(suggestionsPointer).getUpdatedSuggestionLocally();
    }

    public class SuggestionDao {
        @Nonnull
        private Observable<ResponseOrError<SuggestionsResponse>> suggestionsObservable;
        @Nonnull
        private final PublishSubject<ResponseOrError<SuggestionsResponse>> updatedSuggestionLocally = PublishSubject.create();

        public SuggestionDao(@Nullable SuggestionsPointer suggestionsPointer) {
            final UserLocation userLocation = suggestionsPointer.getUserLocation();

            final Observable<SuggestionsResponse> request;
            if (userLocation != null) {
                request = apiService.suggestions(userLocation.getCountry(), userLocation.getState(), userLocation.getCity(), 1, suggestionsPointer.getPageSize());
            } else {
                request = apiService.suggestions(null, null, null, 1, suggestionsPointer.getPageSize());
            }

            suggestionsObservable = request
                    .subscribeOn(networkScheduler)
                    .compose(ResponseOrError.<SuggestionsResponse>toResponseOrErrorObservable())
                    .mergeWith(updatedSuggestionLocally)
                    .compose(MoreOperators.<ResponseOrError<SuggestionsResponse>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<SuggestionsResponse>> getSuggestionsObservable() {
            return suggestionsObservable;
        }

        @Nonnull
        public PublishSubject<ResponseOrError<SuggestionsResponse>> getUpdatedSuggestionLocally() {
            return updatedSuggestionLocally;
        }
    }

    public static class SuggestionsPointer {
        private final int pageSize;
        @Nullable
        private final UserLocation userLocation;

        public SuggestionsPointer(int pageSize, @Nullable UserLocation userLocation) {
            this.pageSize = pageSize;
            this.userLocation = userLocation;
        }

        public int getPageSize() {
            return pageSize;
        }

        @Nullable
        public UserLocation getUserLocation() {
            return userLocation;
        }
    }

}
