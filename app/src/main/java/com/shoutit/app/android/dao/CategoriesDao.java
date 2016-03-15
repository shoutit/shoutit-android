package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Category;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;

public class CategoriesDao {

    private final Observable<ResponseOrError<List<Category>>> mListObservableResponseOrError;

    @Inject
    public CategoriesDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        mListObservableResponseOrError = apiService
                .categories()
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<List<Category>>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<List<Category>>>cacheWithTimeout(networkScheduler));
    }

    @NonNull
    public Observable<ResponseOrError<List<Category>>> getListObservableResponseOrError() {
        return mListObservableResponseOrError;
    }
}
