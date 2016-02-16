package com.shoutit.app.android.utils;


import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.appunite.rx.dagger.NetworkScheduler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.dagger.ForApplication;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func0;

public class LocationUtils {

    private static final LatLngBounds BOUNDS_WORLDWIDE = new LatLngBounds(
            new LatLng(-85, -180),       // south-west
            new LatLng(85, 180)        // north-east
    );

    public static boolean isLocationDifferenceMoreThanDelta(double originLat, double originLng,
                                                            double newLat, double newLng, double deltaInMeters) {
        final float[] results = new float[] {0.0f};
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
        return String.format(Locale.US,  "%1$f,%2$f", latitude, longitude);
    }

    @Nonnull
    public static Observable<Location> getLocationObservable(@Nonnull final GoogleApiClient googleApiClient,
                                                             @Nonnull @ForApplication final Context context,
                                                             @Nonnull @NetworkScheduler Scheduler networkScheduler) {
        return Observable
                .fromCallable(new Func0<Location>() {
                    @Override
                    public Location call() {
                        if (!googleApiClient.isConnected()) {
                            googleApiClient.blockingConnect();
                        }

                        if (!PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            return null;
                        }

                        //noinspection MissingPermission
                        final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                        if (BuildConfig.DEBUG && lastLocation != null) {
                            Log.d("LocationManager", "received lat lng: " + lastLocation.getLatitude() + " " + lastLocation.getLongitude());
                        }

                        return lastLocation;
                    }
                })
                .subscribeOn(networkScheduler);
    }
}
