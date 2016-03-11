package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.shoutit.app.android.api.model.ShoutsResponse;


import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public abstract class BaseShoutsDao {

    @Nonnull
    private Observable<ResponseOrError<ShoutsResponse>> shoutsObservable;
    @Nonnull
    protected final PublishSubject<Object> loadMoreShoutsSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<Object> refreshShoutsSubject = PublishSubject.create();

    public BaseShoutsDao(final @Nonnull @NetworkScheduler Scheduler networkScheduler) {
        final OperatorMergeNextToken<ShoutsResponse, Object> loadMoreOperator =
                OperatorMergeNextToken.create(new Func1<ShoutsResponse, Observable<ShoutsResponse>>() {
                    private int pageNumber = 0;

                    @Override
                    public Observable<ShoutsResponse> call(ShoutsResponse previousResponse) {
                        if (previousResponse == null || previousResponse.getNext() != null) {
                            if (previousResponse == null) {
                                pageNumber = 0;
                            }
                            ++pageNumber;

                            final Observable<ShoutsResponse> apiRequest = getShoutsRequest(pageNumber)
                                    .subscribeOn(networkScheduler);

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
                .compose(MoreOperators.refresh(refreshShoutsSubject))
                .lift(loadMoreOperator)
                .compose(ResponseOrError.<ShoutsResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<ShoutsResponse>>cacheWithTimeout(networkScheduler));
    }


    @Nonnull
    public Observable<ResponseOrError<ShoutsResponse>> getShoutsObservable() {
        return shoutsObservable;
    }

    @Nonnull
    public Observer<Object> getRefreshObserver() {
        return refreshShoutsSubject;
    }

    @NonNull
    @Nonnull
    abstract Observable<ShoutsResponse> getShoutsRequest(int pageNumber);
}