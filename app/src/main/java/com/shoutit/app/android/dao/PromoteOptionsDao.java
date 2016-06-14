package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.PromoteOption;

import java.util.List;

import rx.Observable;
import rx.Scheduler;


public class PromoteOptionsDao {

    @NonNull
    private final ApiService apiService;
    @NonNull
    private final Scheduler networkScheduler;
    @NonNull
    private final Observable<ResponseOrError<List<PromoteOption>>> optionsObservable;

    public PromoteOptionsDao(@NonNull ApiService apiService,
                             @NonNull @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        optionsObservable = apiService.promoteOptions()
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.toResponseOrErrorObservable())
                .compose(MoreOperators.cacheWithTimeout(networkScheduler));
    }

    @NonNull
    public Observable<ResponseOrError<List<PromoteOption>>> getOptionsObservable() {
        return optionsObservable;
    }
}

