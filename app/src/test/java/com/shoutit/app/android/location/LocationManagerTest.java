package com.shoutit.app.android.location;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UpdateLocationRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.LocationUtils;
import com.shoutit.app.android.utils.PermissionHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.BehaviorSubject;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PermissionHelper.class, LocationUtils.class})
public class LocationManagerTest {

    @Mock
    Context context;
    @Mock
    ApiService apiService;
    @Mock
    UserPreferences userPreferences;
    @Mock
    GoogleApiClient googleApiClient;
    @Mock
    User user;
    @Mock
    android.location.Location gpsLocation;

    private BehaviorSubject<UserLocation> locationFromGpsSubject = BehaviorSubject.create();
    private BehaviorSubject<UserLocation> locationFromIPSubject = BehaviorSubject.create();

    private TestScheduler testScheduler = new TestScheduler();
    private LocationManager locationManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(PermissionHelper.class);
        PowerMockito.mockStatic(LocationUtils.class);

        when(userPreferences.isUserLoggedIn()).thenReturn(true);
        when(userPreferences.automaticLocationTrackingEnabled()).thenReturn(true);
        when(userPreferences.getLocation()).thenReturn(getCurrentLocation());

        when(apiService.geocode(anyString())).thenReturn(locationFromGpsSubject);
        when(apiService.geocodeDefault()).thenReturn(locationFromIPSubject);
        when(apiService.updateUserLocation(any(UpdateLocationRequest.class))).thenReturn(Observable.just(user));

        when(gpsLocation.getLatitude()).thenReturn(1d);
        when(gpsLocation.getLongitude()).thenReturn(2d);

        when(LocationUtils.getLocationObservable(any(GoogleApiClient.class), any(Context.class), any(Scheduler.class)))
                .thenReturn(Observable.just(gpsLocation));

        when(PermissionHelper.hasPermission(any(Context.class), anyString())).thenReturn(true);
        when(LocationUtils.isLocationDifferenceMoreThanDelta(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(true);

        when(googleApiClient.blockingConnect()).thenReturn(new ConnectionResult(0));

        locationManager = new LocationManager(context, userPreferences, googleApiClient, apiService, testScheduler);

    }

    @Test
    public void testWhenSubscribed_locationFromGpsFetched() throws Exception {
        final TestSubscriber<UserLocation> subscriber = new TestSubscriber<>();
        when(userPreferences.automaticLocationTrackingEnabled()).thenReturn(true);
        final UserLocation location = getLocationWithLatLngCity(1, 2, "city");

        locationManager.updateUserLocationObservable().subscribe(subscriber);

        locationFromGpsSubject.onNext(location);
        testScheduler.triggerActions();

        subscriber.assertValueCount(1);
        subscriber.assertValue(location);
    }

    @Test
    public void testWhenRefreshedSubject_locationFromGpsFetchedAgain() throws Exception {
        final TestSubscriber<UserLocation> subscriber = new TestSubscriber<>();
        when(userPreferences.automaticLocationTrackingEnabled()).thenReturn(true);
        final UserLocation location = getLocationWithLatLngCity(1, 2, "city");

        locationManager.updateUserLocationObservable().subscribe(subscriber);

        locationFromGpsSubject.onNext(location);
        testScheduler.triggerActions();
        subscriber.assertValueCount(1);
        locationManager.getRefreshGetLocationSubject().onNext(null);
        testScheduler.triggerActions();

        subscriber.assertValueCount(2);
        subscriber.assertValues(location, location);
    }

    @Test
    public void testWhenSubscribed_locationFromIPFetched() throws Exception {
        final TestSubscriber<UserLocation> subscriber = new TestSubscriber<>();
        when(userPreferences.automaticLocationTrackingEnabled()).thenReturn(true);
        when(PermissionHelper.hasPermission(any(Context.class), anyString())).thenReturn(false);
        final UserLocation location = getLocationWithLatLngCity(1, 2, "city");

        locationManager.updateUserLocationObservable().subscribe(subscriber);

        locationFromIPSubject.onNext(location);
        testScheduler.triggerActions();

        subscriber.assertValueCount(1);
        subscriber.assertValue(location);
    }

    @Test
    public void testWhenRefreshedSubject_locationFromIpFetchedAgain() throws Exception {
        final TestSubscriber<UserLocation> subscriber = new TestSubscriber<>();
        when(userPreferences.automaticLocationTrackingEnabled()).thenReturn(true);
        when(PermissionHelper.hasPermission(any(Context.class), anyString())).thenReturn(false);
        final UserLocation location = getLocationWithLatLngCity(1, 2, "city");

        locationManager.updateUserLocationObservable().subscribe(subscriber);

        locationFromIPSubject.onNext(location);
        testScheduler.triggerActions();
        locationManager.getRefreshGetLocationSubject().onNext(null);
        testScheduler.triggerActions();

        subscriber.assertValueCount(2);
        subscriber.assertValues(location, location);
    }

    @Test
    public void testWhenLocationChangedAndUserLoggedIn_userUpdated() throws Exception {
        final TestSubscriber<UserLocation> subscriber = new TestSubscriber<>();
        when(userPreferences.automaticLocationTrackingEnabled()).thenReturn(true);
        when(userPreferences.isUserLoggedIn()).thenReturn(true);
        when(PermissionHelper.hasPermission(any(Context.class), anyString())).thenReturn(false);
        final UserLocation location = getLocationWithLatLngCity(1, 2, "city");

        locationManager.updateUserLocationObservable().subscribe(subscriber);

        locationFromIPSubject.onNext(location);
        testScheduler.triggerActions();

        subscriber.assertValueCount(1);
        verify(apiService, times(1)).updateUserLocation(any(UpdateLocationRequest.class));
        verify(userPreferences, times(1)).setUser(any(User.class));
    }

    @Test
    public void testWhenLocationChangedAndUserNotLoggedIn_userNotUpdated() throws Exception {
        final TestSubscriber<UserLocation> subscriber = new TestSubscriber<>();
        when(userPreferences.automaticLocationTrackingEnabled()).thenReturn(true);
        when(userPreferences.isUserLoggedIn()).thenReturn(false);
        when(PermissionHelper.hasPermission(any(Context.class), anyString())).thenReturn(false);
        final UserLocation location = getLocationWithLatLngCity(1, 2, "city");

        locationManager.updateUserLocationObservable().subscribe(subscriber);

        locationFromIPSubject.onNext(location);
        testScheduler.triggerActions();

        subscriber.assertValueCount(1);
        verify(apiService, times(0)).updateUserLocation(any(UpdateLocationRequest.class));
        verify(userPreferences, times(0)).setUser(any(User.class));
    }

    private UserLocation getLocationWithLatLngCity(float lat, float lng, String city) {
        return new UserLocation(lat, lng, null, null, null, city, null);
    }

    @Nonnull
    private UserLocation getCurrentLocation() {
        return new UserLocation(5, 6, "z", null, null, null, null);
    }
}
