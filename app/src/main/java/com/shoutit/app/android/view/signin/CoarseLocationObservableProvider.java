package com.shoutit.app.android.view.signin;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.shoutit.app.android.rx.CoarseLocationObservable;

import rx.Observable;

public interface CoarseLocationObservableProvider {

    CoarseLocationObservableProvider DEFAULT = new CoarseLocationObservableProvider() {
        @Override
        public Observable<Location> get(@NonNull Context context) {
            return CoarseLocationObservable.get(context);
        }
    };

    Observable<Location> get(@NonNull Context context);
}