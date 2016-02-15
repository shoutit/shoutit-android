package com.shoutit.app.android.location;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.base.Strings;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UpdateLocationRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.LocationUtils;
import com.shoutit.app.android.utils.PermissionHelper;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

@Singleton
public class LocationManager implements GoogleApiClient.ConnectionCallbacks {

    private static final double LOCATION_MAX_DELTA_METERS = 1000 * 5;

    @Nonnull
    private final BehaviorSubject<android.location.Location> lastGoogleLocationSubject = BehaviorSubject.create();
    @Nonnull
    private final PublishSubject<Object> refreshGetLocationSubject = PublishSubject.create();
    @Nonnull
    private final Observable<UserLocation> updateLocationObservable;

    @Nonnull
    private final Context context;
    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final GoogleApiClient googleApiClient;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;

    public LocationManager(@ForApplication @Nonnull final Context context,
                           @Nonnull final UserPreferences userPreferences,
                           @Nonnull final GoogleApiClient googleApiClient,
                           @Nonnull final ApiService apiService,
                           @Nonnull @NetworkScheduler final Scheduler networkScheduler) {
        this.context = context;
        this.userPreferences = userPreferences;
        this.googleApiClient = googleApiClient;
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        googleApiClient.registerConnectionCallbacks(this);

        final Observable<UserLocation> locationFromCoordinates = lastGoogleLocationSubject
                .filter(Functions1.isNotNull())
                .filter(coordinatesChangedFilter())
                .switchMap(new Func1<android.location.Location, Observable<UserLocation>>() {
                    @Override
                    public Observable<UserLocation> call(android.location.Location location) {
                        return apiService.geocode(location.getLatitude() + "," + location.getLongitude())
                                .subscribeOn(networkScheduler)
                                .compose(ResponseOrError.<UserLocation>toResponseOrErrorObservable())
                                .compose(ResponseOrError.<UserLocation>onlySuccess());
                    }
                });

        final Observable<UserLocation> locationFromIP = apiService.geocodeDefault()
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<UserLocation>toResponseOrErrorObservable())
                .compose(ResponseOrError.<UserLocation>onlySuccess());

        final PublishSubject<UserLocation> locationChangedSubject = PublishSubject.create();
        updateLocationObservable = Observable
                .defer(new Func0<Observable<UserLocation>>() {
                    @Override
                    public Observable<UserLocation> call() {
                        if (userPreferences.automaticLocationTrackingEnabled() && hasLocationPermissions(context)) {
                            googleApiClient.connect();
                            return locationFromCoordinates;
                        } else if (userPreferences.automaticLocationTrackingEnabled()){
                            return locationFromIP;
                        } else {
                            return Observable.never();
                        }
                    }
                })
                .compose(MoreOperators.<UserLocation>refresh(refreshGetLocationSubject))
                .filter(Functions1.isNotNull())
                .filter(locationChangedFilter())
                .lift(MoreOperators.callOnNext(locationChangedSubject))
                .doOnNext(saveToPreferences()); // TODO remove this if guest user will be handled by API

        locationChangedSubject
                .filter(new Func1<UserLocation, Boolean>() {
                    @Override
                    public Boolean call(UserLocation location) {
                        return userPreferences.isUserLoggedIn(); // TODO remove this if guest user will be handled by API
                    }
                })
                .switchMap(updateUserWithNewLocation())
                .subscribe();
    }

    @NonNull
    private Func1<UserLocation, Observable<User>> updateUserWithNewLocation() {
        return new Func1<UserLocation, Observable<User>>() {
            @Override
            public Observable<User> call(UserLocation location) {
                return apiService.updateUserLocation(new UpdateLocationRequest(location))
                        .subscribeOn(networkScheduler)
                        .compose(ResponseOrError.<User>toResponseOrErrorObservable())
                        .compose(ResponseOrError.<User>onlySuccess())
                        .doOnNext(new Action1<User>() {
                            @Override
                            public void call(User user) {
                                userPreferences.saveUserAsJson(user);
                                if (user.getLocation() != null) {
                                    userPreferences.saveLocation(user.getLocation());
                                }
                            }
                        });
            }
        };
    }

    private Func1<UserLocation, Boolean> locationChangedFilter() {
        return new Func1<UserLocation, Boolean>() {
            @Override
            public Boolean call(@Nonnull UserLocation location) {
                final UserLocation currentLocation = userPreferences.getLocation();
                return currentLocation == null ||
                        (currentLocation != null && !Strings.nullToEmpty(currentLocation.getCity()).equalsIgnoreCase(location.getCity()));
            }
        };
    }

    private Func1<android.location.Location, Boolean> coordinatesChangedFilter() {
        return new Func1<android.location.Location, Boolean>() {
            @Override
            public Boolean call(@Nonnull android.location.Location location) {
                final UserLocation currentLocation = userPreferences.getLocation();
                if (currentLocation == null) {
                    return true;
                }

                return LocationUtils.isLocationDifferenceMoreThanDelta(currentLocation.getLatitude(),
                        currentLocation.getLongitude(), location.getLatitude(), location.getLongitude(),
                        LOCATION_MAX_DELTA_METERS);
            }
        };
    }

    private Action1<UserLocation> saveToPreferences() {
        return new Action1<UserLocation>() {
            @Override
            public void call(UserLocation location) {
                userPreferences.saveLocation(location);
            }
        };
    }

    private boolean hasLocationPermissions(@ForApplication Context context) {
        return PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        if (!hasLocationPermissions(context)) {
            return;
        }

        android.location.Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        lastGoogleLocationSubject.onNext(lastLocation);
        if (BuildConfig.DEBUG && lastLocation != null) {
            Log.d("LocationManager", "received lat lng: " + lastLocation.getLatitude() + " " + lastLocation.getLongitude());
        }
        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Nonnull
    public PublishSubject<Object> getRefreshGetLocationSubject() {
        return refreshGetLocationSubject;
    }

    @Nonnull
    public Observable<UserLocation> updateUserLocationObservable() {
        return updateLocationObservable;
    }

    @Nonnull
    public BehaviorSubject<android.location.Location> getLastGoogleLocationSubject() {
        return lastGoogleLocationSubject;
    }
}
