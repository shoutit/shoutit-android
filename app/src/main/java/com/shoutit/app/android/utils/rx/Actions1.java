package com.shoutit.app.android.utils.rx;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;

import rx.Observer;
import rx.functions.Action1;

public class Actions1 {

    @NonNull
    public static Action1<String> showError(@NonNull final TextInputLayout textInputLayout, @NonNull final String message) {
        return new Action1<String>() {
            @Override
            public void call(String s) {
                textInputLayout.setError(message);
            }
        };
    }

    @NonNull
    public static Action1<Object> hideError(@NonNull final TextInputLayout textInputLayout) {
        return new Action1<Object>() {
            @Override
            public void call(Object s) {
                textInputLayout.setError(null);
                textInputLayout.setErrorEnabled(false);
            }
        };
    }

    @NonNull
    public static Action1<Boolean> showOrHideError(@NonNull final TextInputLayout textInputLayout, @NonNull final String message) {
        return new Action1<Boolean>() {
            @Override
            public void call(Boolean show) {
                if (show) {
                    textInputLayout.setError(message);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }
        };
    }

    @NonNull
    public static Action1<Boolean> setOrEraseError(@NonNull final TextInputLayout textInputLayout, @NonNull final String message) {
        return new Action1<Boolean>() {
            @Override
            public void call(Boolean setError) {
                if (setError) {
                    textInputLayout.setError(message);
                } else {
                    textInputLayout.setError(null);
                }
            }
        };
    }

    public static <T> Action1<T> progressOnNext(@NonNull final Observer<Boolean> progressObserver,
                                                final boolean showProgress) {
        return new Action1<T>() {
            @Override
            public void call(T ignore) {
                progressObserver.onNext(showProgress);
            }
        };
    }

}
