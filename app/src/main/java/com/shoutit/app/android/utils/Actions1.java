package com.shoutit.app.android.utils;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;

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
                    textInputLayout.setErrorEnabled(false);
                }
            }
        };
    }

}
