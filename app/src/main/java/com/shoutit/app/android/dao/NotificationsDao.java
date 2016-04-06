package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.NotificationsResponse;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.subjects.PublishSubject;


public class NotificationsDao {

    @NonNull
    private final Observable<ResponseOrError<NotificationsResponse>> notificationsObservable;
    @NonNull
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();

    public NotificationsDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        notificationsObservable = apiService
                .notifications()
                .subscribeOn(networkScheduler)
                .compose(MoreOperators.<NotificationsResponse>refresh(refreshSubject))
                .compose(ResponseOrError.<NotificationsResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<NotificationsResponse>>cacheWithTimeout(networkScheduler));
    }

    @NonNull
    public Observable<ResponseOrError<NotificationsResponse>> getNotificationsObservable() {
        return notificationsObservable;
    }

    @NonNull
    public Observer<Object> getRefreshObserver() {
        return refreshSubject;
    }
}

