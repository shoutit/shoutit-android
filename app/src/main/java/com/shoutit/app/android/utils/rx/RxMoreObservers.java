package com.shoutit.app.android.utils.rx;

import javax.annotation.Nonnull;

import rx.Observer;

import static com.google.common.base.Preconditions.checkNotNull;

public class RxMoreObservers {
    @Nonnull
    public static <T> Observer<T> ignoreCompleted(@Nonnull final Observer<T> o) {
        checkNotNull(o);
        return new Observer<T>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                o.onError(e);
            }

            @Override
            public void onNext(T t) {
                o.onNext(t);
            }
        };
    }
}