package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.SortType;

import java.util.List;

import rx.Observable;
import rx.Scheduler;

public class SortTypesDao {

    private final Observable<ResponseOrError<List<SortType>>> sortTypesObservable;

    public SortTypesDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        sortTypesObservable = apiService
                .sortTypes()
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<List<SortType>>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<List<SortType>>>cacheWithTimeout(networkScheduler));
    }

    @NonNull
    public Observable<ResponseOrError<List<SortType>>> sortTypesObservable() {
        return sortTypesObservable;
    }

}
