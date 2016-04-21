package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.model.MergeListeningResponses;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ListeningsDao {

    private static final int PAGE_SIZE = 20;

    @Nonnull
    private final Observable<ResponseOrError<ListeningResponse>> listeningObservable;
    @Nonnull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();

    public ListeningsDao(final ApiService apiService, @NetworkScheduler final Scheduler networkScheduler) {

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
                                    .listenings(pageNumber, PAGE_SIZE)
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

    @NonNull
    public Observable<ResponseOrError<ListeningResponse>> getLsteningObservable() {
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
