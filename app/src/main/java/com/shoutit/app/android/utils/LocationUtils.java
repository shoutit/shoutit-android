package com.shoutit.app.android.utils;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.common.base.Objects;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.dagger.ForApplication;

import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

public class LocationUtils {

    private static final LatLngBounds BOUNDS_WORLDWIDE = new LatLngBounds(
            new LatLng(-85, -180),       // south-west
            new LatLng(85, 180)        // north-east
    );

    public static boolean isLocationDifferenceMoreThanDelta(double originLat, double originLng,
                                                            double newLat, double newLng, double deltaInMeters) {
        final float[] results = new float[]{0.0f};
        android.location.Location.distanceBetween(originLat, originLng, newLat, newLng, results);

        return results[0] > deltaInMeters;
    }

    public static PendingResult<AutocompletePredictionBuffer> getPredictionsForQuery(
            @Nonnull GoogleApiClient googleApiClient, @Nonnull String query) {
        return Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query,
                BOUNDS_WORLDWIDE, null);
    }

    public static String convertCoordinatesForRequest(double latitude, double longitude) {
        // This locale is required to have dots in double instead comma
        return String.format(Locale.US, "%1$f,%2$f", latitude, longitude);
    }

    @Nonnull
    public static Observable<LocationInfo> getLastLocationObservable(@Nonnull final GoogleApiClient googleApiClient,
                                                                     @Nonnull @ForApplication final Context context,
                                                                     @Nonnull @NetworkScheduler Scheduler networkScheduler) {
        return Observable.fromCallable(new Callable<LocationInfo>() {
            @Override
            public LocationInfo call() throws Exception {
                if (!googleApiClient.isConnected()) {
                    googleApiClient.blockingConnect();
                }

                if (!PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    return new LocationInfo(null, true, false);
                }

                //noinspection MissingPermission
                final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                if (BuildConfig.DEBUG && lastLocation != null) {
                    Log.d("LocationManager", "received lat lng: " + lastLocation.getLatitude() + " " + lastLocation.getLongitude());
                }

                return lastLocation == null ?
                        new LocationInfo(null, false, true) : new LocationInfo(lastLocation, true, true);
            }
        }).subscribeOn(networkScheduler);
    }

    public static Observable<LocationInfo> getLocationFromUpdatesObservable(@Nonnull final GoogleApiClient googleApiClient,
                                                                            @Nonnull @ForApplication final Context context) {
        return Observable.create(subscriber -> {
            if (!googleApiClient.isConnected()) {
                googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                });
                googleApiClient.connect();
            } else {
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).switchMap(o -> Observable.create(new Observable.OnSubscribe<LocationInfo>() {

            private LocationListener locationListener;

            @SuppressWarnings("MissingPermission")
            @Override
            public void call(Subscriber<? super LocationInfo> subscriber) {
                if (!PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(new LocationInfo(null, true, false));
                    }
                } else {
                    final LocationRequest locationRequest = new LocationRequest()
                            .setInterval(DateUtils.MINUTE_IN_MILLIS)
                            .setFastestInterval(8 * DateUtils.SECOND_IN_MILLIS)
                            .setSmallestDisplacement(5000);

                    locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            if (!subscriber.isUnsubscribed()) {
                                final LocationInfo locationInfo = location == null ?
                                        new LocationInfo(null, false, true) : new LocationInfo(location, true, true);
                                subscriber.onNext(locationInfo);
                            }
                        }
                    };

                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);

                    subscriber.add(Subscriptions.create(() -> {
                        if (locationListener != null) {
                            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
                        }
                    }));
                }
            }
        })).subscribeOn(MyAndroidSchedulers.mainThread());

    }

    public static class LocationInfo {
        @Nullable
        private final Location location;
        private final boolean isLocationServiceEnabled;
        private final boolean hasPermissions;

        public LocationInfo(@Nullable Location location, boolean isLocationServiceEnabled, boolean hasPermissions) {
            this.location = location;
            this.isLocationServiceEnabled = isLocationServiceEnabled;
            this.hasPermissions = hasPermissions;
        }

        @Nullable
        public Location getLocation() {
            return location;
        }

        public boolean isLocationServiceEnabled() {
            return isLocationServiceEnabled;
        }

        public boolean isHasPermissions() {
            return hasPermissions;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LocationInfo)) return false;
            final LocationInfo that = (LocationInfo) o;
            return isLocationServiceEnabled == that.isLocationServiceEnabled &&
                    hasPermissions == that.hasPermissions &&
                    Objects.equal(location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(location, isLocationServiceEnabled, hasPermissions);
        }
    }
}
