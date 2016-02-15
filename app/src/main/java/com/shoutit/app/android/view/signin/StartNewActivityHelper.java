package com.shoutit.app.android.view.signin;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.shoutit.app.android.view.main.MainActivity;

import rx.functions.Action1;

public class StartNewActivityHelper {

    private static void startActivityAfterAuth(@NonNull Activity activity) {
        final Intent intent = activity.getIntent();

        final boolean guestLogin = intent.getBooleanExtra(LoginActivity.EXTRAS_GUEST_LOGIN, false);

        if (guestLogin) {
            final Class<?> clazz = (Class<?>) intent.getSerializableExtra(LoginActivity.EXTRAS_CALLING_CLASS);
            ActivityCompat.finishAffinity(activity);
            activity.startActivity(new Intent(activity, clazz));
        } else {
            ActivityCompat.finishAffinity(activity);
            activity.startActivity(MainActivity.newIntent(activity));
        }
    }

    public static Action1<Object> startActivityAfterAuthAction(@NonNull final Activity activity) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                startActivityAfterAuth(activity);
            }
        };
    }
}