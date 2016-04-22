package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.NotificationsResponse;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;


public class NotificationsDao {

    private static final int PAGE_SIZE = 20;

    @NonNull
    private final Observable<ResponseOrError<NotificationsResponse>> notificationsObservable;
    @NonNull
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();
    @NonNull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();

    public NotificationsDao(final ApiService apiService, @NetworkScheduler final Scheduler networkScheduler) {
        final OperatorMergeNextToken<NotificationsResponse, Object> loadMoreOperator =
                OperatorMergeNextToken.create(new Func1<NotificationsResponse, Observable<NotificationsResponse>>() {
                    private Integer beforeTimestamp = null;

                    @Override
                    public Observable<NotificationsResponse> call(NotificationsResponse previousResponse) {
                        if (previousResponse == null || previousResponse.getPrevious() != null) {
                            if (previousResponse == null) {
                                beforeTimestamp = null;
                            } else {
                                beforeTimestamp = Iterables.getLast(previousResponse.getResults())
                                        .getCreatedAt();
                            }

                            final Observable<NotificationsResponse> apiRequest = apiService
                                    .notifications(beforeTimestamp, PAGE_SIZE)
                                    .subscribeOn(networkScheduler);

                            if (previousResponse == null) {
                                return apiRequest;
                            } else {
                                return Observable.just(previousResponse).zipWith(apiRequest, new MergeNotificationsResponses());
                            }
                        } else {
                            return Observable.never();
                        }
                    }
                });

        notificationsObservable = loadMoreSubject.startWith((Object) null)
                .lift(loadMoreOperator)
                .compose(ResponseOrError.<NotificationsResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<NotificationsResponse>>refresh(refreshSubject))
                .compose(MoreOperators.<ResponseOrError<NotificationsResponse>>cacheWithTimeout(networkScheduler));
    }

    @NonNull
    public Observable<ResponseOrError<NotificationsResponse>> getNotificationsObservable() {
        return notificationsObservable;
    }

    @NonNull
    public Observer<Object> getLoadMoreObserver() {
        return loadMoreSubject;
    }

    @NonNull
    public Observer<Object> getRefreshObserver() {
        return refreshSubject;
    }

    private class MergeNotificationsResponses implements Func2<NotificationsResponse, NotificationsResponse, NotificationsResponse> {
        @Override
        public NotificationsResponse call(NotificationsResponse previousResponses, NotificationsResponse lastResponse) {
            final ImmutableList<NotificationsResponse.Notification> items = ImmutableList.<NotificationsResponse.Notification>builder()
                    .addAll(previousResponses.getResults())
                    .addAll(lastResponse.getResults())
                    .build();

            return new NotificationsResponse(lastResponse.getCount(), lastResponse.getNext(), lastResponse.getPrevious(), items);
        }
    }
}

