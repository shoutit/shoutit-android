package com.shoutit.app.android.location;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.appunite.rx.ObservableExtensions;
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
import com.shoutit.app.android.api.model.Location;
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

    private static final double LOCATION_MAX_DELTA_METERS = 1000 * 20;

    @Nonnull
    private final BehaviorSubject<android.location.Location> lastGoogleLocationSubject = BehaviorSubject.create();
    @Nonnull
    private final PublishSubject<Object> refreshGetLocationSubject = PublishSubject.create();
    @Nonnull
    private final Observable<Location> updateLocationObservable;

    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final GoogleApiClient googleApiClient;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;

    public LocationManager(@ForApplication final Context context,
                           @Nonnull final UserPreferences userPreferences,
                           @Nonnull final GoogleApiClient googleApiClient,
                           @Nonnull final ApiService apiService,
                           @Nonnull @NetworkScheduler final Scheduler networkScheduler) {
        this.userPreferences = userPreferences;
        this.googleApiClient = googleApiClient;
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        googleApiClient.registerConnectionCallbacks(this);

        final Observable<Location> locationFromCoordinates = lastGoogleLocationSubject
                .filter(Functions1.isNotNull())
                .filter(coordinatesChangedFilter())
                .switchMap(new Func1<android.location.Location, Observable<Location>>() {
                    @Override
                    public Observable<Location> call(android.location.Location location) {
                        return apiService.geocode(location.getLatitude() + "," + location.getLongitude())
                                .subscribeOn(networkScheduler)
                                .compose(ResponseOrError.<Location>toResponseOrErrorObservable())
                                .compose(ResponseOrError.<Location>onlySuccess());
                    }
                });

        final Observable<Location> locationFromIP = apiService.geocodeDefault()
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<Location>toResponseOrErrorObservable())
                .compose(ResponseOrError.<Location>onlySuccess());

        final PublishSubject<Location> locationChangedSubject = PublishSubject.create();
        updateLocationObservable = Observable
                .defer(new Func0<Observable<Location>>() {
                    @Override
                    public Observable<Location> call() {
                        if (userPreferences.automaticLocationTrackingEnabled() && hasLocationPermissions(context)) {
                            googleApiClient.connect();
                            return locationFromCoordinates;
                        } else {
                            return locationFromIP;
                        }
                    }
                })
                .compose(MoreOperators.<Location>refresh(refreshGetLocationSubject))
                .filter(Functions1.isNotNull())
                .filter(locationChangedFilter())
                .lift(MoreOperators.callOnNext(locationChangedSubject))
                .doOnNext(saveToPreferences());

        locationChangedSubject
                .filter(new Func1<Location, Boolean>() {
                    @Override
                    public Boolean call(Location location) {
                        return userPreferences.isUserLoggedIn();
                    }
                })
                .switchMap(updateUserWithNewLocation())
                .subscribe();
    }

    @NonNull
    private Func1<Location, Observable<User>> updateUserWithNewLocation() {
        return new Func1<Location, Observable<User>>() {
            @Override
            public Observable<User> call(Location location) {
                return apiService.updateUserLocation(new UpdateLocationRequest(location))
                        .subscribeOn(networkScheduler)
                        .compose(ResponseOrError.<User>toResponseOrErrorObservable())
                        .compose(ResponseOrError.<User>onlySuccess())
                        .doOnNext(new Action1<User>() {
                            @Override
                            public void call(User user) {
                                userPreferences.saveUserAsJson(user);
                            }
                        });
            }
        };
    }

    private Func1<Location, Boolean> locationChangedFilter() {
        return new Func1<Location, Boolean>() {
            @Override
            public Boolean call(@Nonnull Location location) {
                final Location currentLocation = userPreferences.getLocation();
                return currentLocation == null ||
                        (currentLocation != null && !Strings.nullToEmpty(currentLocation.getCity()).equalsIgnoreCase(location.getCity()));
            }
        };
    }

    private Func1<android.location.Location, Boolean> coordinatesChangedFilter() {
        return new Func1<android.location.Location, Boolean>() {
            @Override
            public Boolean call(@Nonnull android.location.Location location) {
                final Location currentLocation = userPreferences.getLocation();

                return LocationUtils.isLocationDifferenceMoreThanDelta(currentLocation.getLatitude(), 
                        currentLocation.getLongitude(), location.getLatitude(), location.getLongitude(),
                        LOCATION_MAX_DELTA_METERS);
            }
        };
    }

    private Action1<Location> saveToPreferences() {
        return new Action1<Location>() {
            @Override
            public void call(Location location) {
                userPreferences.saveLocation(location);
            }
        };
    }

    private boolean hasLocationPermissions(@ForApplication Context context) {
        return PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onConnected(Bundle bundle) {
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
    public Observable<Location> updateUserLocationObservable() {
        return updateLocationObservable;
    }

    @Nonnull
    public BehaviorSubject<android.location.Location> getLastGoogleLocationSubject() {
        return lastGoogleLocationSubject;
    }
}
