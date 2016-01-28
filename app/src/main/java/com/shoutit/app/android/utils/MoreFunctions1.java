package com.shoutit.app.android.utils;

import rx.Observable;
import rx.functions.Func1;

public class MoreFunctions1 {

    public static <T, R> Func1<T, Observable<R>> returnObservable(final Observable<R> toReturn) {
        return new Func1<T, Observable<R>>() {
            @Override
            public Observable<R> call(T t) {
                return toReturn;
            }
        };
    }

}
