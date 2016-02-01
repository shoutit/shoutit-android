package com.shoutit.app.android.utils;

import android.support.annotation.NonNull;
import android.widget.EditText;

import rx.functions.Action1;

public class Actions1 {

    @NonNull
    public static Action1<String> showError(@NonNull  final EditText editText, @NonNull final String message) {
        return new Action1<String>() {
            @Override
            public void call(String s) {
                editText.setError(message);
            }
        };
    }

}
