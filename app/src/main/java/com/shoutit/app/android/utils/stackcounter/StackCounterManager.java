package com.shoutit.app.android.utils.stackcounter;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import rx.Observable;

public class StackCounterManager {

    private final StackCounter mStackCounter;

    @Inject
    public StackCounterManager(@NonNull StackCounter stackCounter) {
        mStackCounter = stackCounter;
    }

    @NonNull
    private Application.ActivityLifecycleCallbacks getCallbacks() {
        return new LifecycleCallbacksAdapter() {
            @Override
            public void onActivityResumed(Activity activity) {
                mStackCounter.onActivityResumed();
            }

            @Override
            public void onActivityPaused(Activity activity) {
                mStackCounter.onActivityPaused();
            }
        };
    }

    @NonNull
    public Observable<Boolean> register(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(getCallbacks());
        return mStackCounter.getSubject();
    }
}
