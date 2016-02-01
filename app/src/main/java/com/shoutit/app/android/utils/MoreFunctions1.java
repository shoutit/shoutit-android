package com.shoutit.app.android.utils;

import android.support.annotation.NonNull;

import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

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

    @NonNull
    public static Func1<TextViewTextChangeEvent, String> mapTextChangeEventToString() {
        return new Func1<TextViewTextChangeEvent, String>() {
            @Override
            public String call(TextViewTextChangeEvent textViewTextChangeEvent) {
                return textViewTextChangeEvent.text().toString();
            }
        };
    }

}
