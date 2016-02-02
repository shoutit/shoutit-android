package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.constants.RequestsConstants;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

@Singleton
public class ShoutsDao {
    private final static int PAGE_SIZE = 20;

    @Nonnull
    private final PublishSubject<Object> loadMoreShouts = PublishSubject.create();
    private final Observable<ResponseOrError<ShoutsResponse>> homeShoutsObservable;

    public ShoutsDao(@Nonnull final ApiService apiService,
                     @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                     @Nonnull final UserPreferences userPreferences) {

        final OperatorMergeNextToken<ShoutsResponse, Object> homeShoutsLoadMoreOperator =
                OperatorMergeNextToken.create(new Func1<ShoutsResponse, Observable<ShoutsResponse>>() {
                    private int pageNumber = 0;

                    @Override
                    public Observable<ShoutsResponse> call(ShoutsResponse previousResponse) {
                        if (previousResponse == null || previousResponse.getNext() != null) {
                            ++pageNumber;
                            final Observable<ShoutsResponse> apiRequest;
                            if (userPreferences.isUserLoggedIn()) {
                                apiRequest = apiService
                                        .home(RequestsConstants.USER_ME, pageNumber, PAGE_SIZE)
                                        .subscribeOn(networkScheduler);
                            } else {
                                apiRequest = apiService
                                        .shoutsForCountry(userPreferences.getUserCountryCode(), pageNumber, PAGE_SIZE)
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

        homeShoutsObservable = loadMoreShouts.startWith((Object) null)
                .lift(homeShoutsLoadMoreOperator)
                .compose(ResponseOrError.<ShoutsResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<ShoutsResponse>>cacheWithTimeout(networkScheduler));
    }

    private class MergeShoutsResponses implements Func2<ShoutsResponse, ShoutsResponse, ShoutsResponse> {
        @Override
        public ShoutsResponse call(ShoutsResponse previousData, ShoutsResponse newData) {
            final ImmutableList<Shout> allItems = ImmutableList.<Shout>builder()
                    .addAll(previousData.getShouts())
                    .addAll(newData.getShouts())
                    .build();

            final int count = previousData.getCount() + newData.getCount();
            return new ShoutsResponse(count, newData.getNext(), newData.getPrevious(), allItems);
        }
    }

    @Nonnull
    public Observer<Object> getLoadMoreShoutsObserver() {
        return loadMoreShouts;
    }

    @Nonnull
    public Observable<ResponseOrError<ShoutsResponse>> getHomeShoutsObservable() {
        return homeShoutsObservable;
    }
}
