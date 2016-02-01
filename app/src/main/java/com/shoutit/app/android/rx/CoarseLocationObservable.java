package com.shoutit.app.android.rx;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class CoarseLocationObservable {

    public static Observable<Location> get(final Context context) {
        return Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(final Subscriber<? super Location> subscriber) {
                final GoogleApiClient apiClient = new GoogleApiClient.Builder(context)
                        .addApi(LocationServices.API)
                        .build();

                apiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if (!subscriber.isUnsubscribed()) {
                            if (checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                                    checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                                final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
                                if (lastLocation != null) {
                                    subscriber.onNext(lastLocation);
                                }
                            }
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                });

                apiClient.connect();

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        apiClient.disconnect();
                    }
                }));
            }
        });
    }

    private static boolean checkPermission(Context context, String accessFineLocation) {
        return ActivityCompat.checkSelfPermission(context, accessFineLocation) == PackageManager.PERMISSION_GRANTED;
    }
}
