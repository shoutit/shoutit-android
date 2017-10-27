package com.shoutit.app.android.utils;

import android.support.annotation.NonNull;

import com.appunite.rx.functions.Functions1;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class MoreFunctions1 {

    public static <T, R> Func1<T, Observable<R>> returnObservableFirst(final Observable<R> toReturn) {
        return new Func1<T, Observable<R>>() {
            @Override
            public Observable<R> call(T t) {
                return toReturn.take(1);
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

    public static Func1<String, Boolean> textNotEmpty() {
        return new Func1<String, Boolean>() {
            @Override
            public Boolean call(String text) {
                return !Strings.isNullOrEmpty(text);
            }
        };
    }

    public static <T> Func1<List<T>, Boolean> listNotEmpty() {
        return new Func1<List<T>, Boolean>() {
            @Override
            public Boolean call(List<T> list) {
                return list != null && !list.isEmpty();
            }
        };
    }

    public static <T> Func1<Optional<T>, Boolean> isPresent() {
        return new Func1<Optional<T>, Boolean>() {
            @Override
            public Boolean call(Optional<T> optional) {
                return optional.isPresent();
            }
        };
    }

    public static <T> Func1<Optional<T>, Boolean> isAbsent() {
        return Functions1.neg(MoreFunctions1.<T>isPresent());
    }

}
