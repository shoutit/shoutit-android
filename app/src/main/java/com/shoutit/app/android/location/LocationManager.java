package com.shoutit.app.android.location;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.common.base.Strings;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.UpdateLocationRequest;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.LocationUtils;
import com.shoutit.app.android.utils.PermissionHelper;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class LocationManager {

    private static final double LOCATION_MAX_DELTA_METERS = 1000 * 5;

    @Nonnull
    private final Observable<UserLocation> updateLocationObservable;

    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;

    public LocationManager(@ForApplication @Nonnull final Context context,
                           @Nonnull final UserPreferences userPreferences,
                           @Nonnull final GoogleApiClient googleApiClient,
                           @Nonnull final ApiService apiService,
                           @Nonnull @NetworkScheduler final Scheduler networkScheduler) {
        this.userPreferences = userPreferences;
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        final Observable<Location> gpsLocationObservable = LocationUtils
                .getLastLocationObservable(googleApiClient, context, networkScheduler)
                .map(LocationUtils.LocationInfo::getLocation)
                .compose(ObservableExtensions.<Location>behaviorRefCount());

        final Observable<UserLocation> successGpsLocationObservable = gpsLocationObservable
                .filter(Functions1.isNotNull())
                .filter(coordinatesChangedFilter())
                .switchMap(location -> apiService.geocode(LocationUtils.convertCoordinatesForRequest(
                        location.getLatitude(), location.getLongitude()))
                        .subscribeOn(networkScheduler)
                        .compose(ResponseOrError.<UserLocation>toResponseOrErrorObservable())
                        .compose(ResponseOrError.<UserLocation>onlySuccess()));

        final PublishSubject<UserLocation> updateUserSubject = PublishSubject.create();
        updateLocationObservable = Observable
                .defer(() -> {
                    if (userPreferences.automaticLocationTrackingEnabled() && hasLocationPermissions(context)) {
                        return successGpsLocationObservable;
                    } else {
                        return Observable.never();
                    }
                })
                .filter(Functions1.isNotNull())
                .filter(locationChangedFilter())
                .lift(MoreOperators.callOnNext(updateUserSubject))
                .doOnNext(saveToPreferences());

        updateUserSubject
                .filter(location -> userPreferences.isUserLoggedIn())
                .switchMap(updateUserWithNewLocation())
                .subscribe();
    }

    @NonNull
    private Func1<UserLocation, Observable<BaseProfile>> updateUserWithNewLocation() {
        return location -> apiService.updateUserLocation(new UpdateLocationRequest(location))
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<BaseProfile>toResponseOrErrorObservable())
                .compose(ResponseOrError.<BaseProfile>onlySuccess())
                .doOnNext(userPreferences::setUserOrPage);
    }

    private Func1<UserLocation, Boolean> locationChangedFilter() {
        return location -> {
            final UserLocation currentLocation = userPreferences.getLocation();
            return currentLocation == null ||
                    !Strings.nullToEmpty(currentLocation.getCity()).equalsIgnoreCase(location.getCity());
        };
    }

    private Func1<android.location.Location, Boolean> coordinatesChangedFilter() {
        return location -> {
            final UserLocation currentLocation = userPreferences.getLocation();
            return currentLocation == null ||
                    LocationUtils.isLocationDifferenceMoreThanDelta(
                            currentLocation.getLatitude(), currentLocation.getLongitude(),
                            location.getLatitude(), location.getLongitude(), LOCATION_MAX_DELTA_METERS);

        };
    }

    private Action1<UserLocation> saveToPreferences() {
        return userPreferences::saveLocation;
    }

    private boolean hasLocationPermissions(@ForApplication Context context) {
        return PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Nonnull
    public Observable<UserLocation> updateUserLocationObservable() {
        return updateLocationObservable;
    }
}
