package com.shoutit.app.android.view.location;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.PermissionHelper;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;


public class LocationPresenter implements GoogleApiClient.ConnectionCallbacks {

    private static final LatLngBounds BOUNDS_WORLDWIDE = new LatLngBounds(
            new LatLng(-85, -180),       // south-west
            new LatLng(85, 180)        // north-east
    );
    private static final long TYPING_THRESHOLD_MS = 500;
    private static final long MINIMUM_SEARCH_INPUT = 3;

    @Nonnull
    private BehaviorSubject<String> querySubject = BehaviorSubject.create();
    @Nonnull
    private final BehaviorSubject<android.location.Location> lastGpsLocationSubject = BehaviorSubject.create();
    @Nonnull
    private PublishSubject<String> locationSelectedSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<Boolean> progressSubject = PublishSubject.create();

    @Nonnull
    private final GoogleApiClient googleApiClient;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final Context context;
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;

    @Inject
    public LocationPresenter(@Nonnull final GoogleApiClient googleApiClient,
                             @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                             @Nonnull @UiScheduler Scheduler uiScheduler,
                             @Nonnull @ForApplication final Context context,
                             @Nonnull final ApiService apiService,
                             @Nonnull final UserPreferences userPreferences) {
        this.googleApiClient = googleApiClient;
        this.uiScheduler = uiScheduler;
        this.context = context;
        googleApiClient.registerConnectionCallbacks(this);
        googleApiClient.connect();

        final Observable<BaseAdapterItem> selectedLocationObservable = userPreferences
                .getLocationObservable()
                .first()
                .filter(Functions1.isNotNull())
                .filter(new Func1<UserLocation, Boolean>() {
                    @Override
                    public Boolean call(UserLocation userLocation) {
                        return !userPreferences.automaticLocationTrackingEnabled();
                    }
                })
                .map(new Func1<UserLocation, BaseAdapterItem>() {
                    @Override
                    public BaseAdapterItem call(UserLocation userLocation) {
                        return new CurrentLocationAdapterItem(
                                userLocation, context.getString(R.string.location_header_selected_location));
                    }
                });

        final Observable<List<BaseAdapterItem>> placesForQueryObservable = querySubject
                .filter(queryFilter())
                .debounce(TYPING_THRESHOLD_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .doOnNext(showProgressAction(true))
                .switchMap(new Func1<String, Observable<AutocompletePredictionBuffer>>() {
                    @Override
                    public Observable<AutocompletePredictionBuffer> call(String query) {
                        final PendingResult<AutocompletePredictionBuffer> results = getResultsForQuery(query);
                        final AutocompletePredictionBuffer predictions = results.await(15, TimeUnit.SECONDS);

                        return Observable.just(predictions);
                    }
                })
                .subscribeOn(networkScheduler)
                .doOnNext(showProgressAction(false))
                .filter(new Func1<AutocompletePredictionBuffer, Boolean>() {
                    @Override
                    public Boolean call(AutocompletePredictionBuffer predictions) {
                        final boolean isSuccess = predictions.getStatus().isSuccess();
                        if (!isSuccess) {
                            predictions.release();
                        }
                        return isSuccess;
                    }
                })
                .map(new Func1<AutocompletePredictionBuffer, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(AutocompletePredictionBuffer predictions) {

                        final Iterator<AutocompletePrediction> iterator = predictions.iterator();
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        while (iterator.hasNext()) {
                            final AutocompletePrediction prediction = iterator.next();
                            builder.add(new PlaceAdapterItem(
                                    prediction.getPlaceId(),
                                    prediction.getFullText(null).toString(),
                                    locationSelectedSubject)
                            );
                        }

                        // Release the buffer now that all data has been copied.
                        predictions.release();

                        return builder.build();
                    }
                });

        final Observable<BaseAdapterItem> currentGpsLocationObservable = lastGpsLocationSubject
                .filter(Functions1.isNotNull())
                .switchMap(new Func1<android.location.Location, Observable<UserLocation>>() {
                    @Override
                    public Observable<UserLocation> call(android.location.Location location) {
                        return apiService.geocode(location.getLatitude() + "," + location.getLongitude())
                                .subscribeOn(networkScheduler)
                                .compose(ResponseOrError.<UserLocation>toResponseOrErrorObservable())
                                .compose(ResponseOrError.<UserLocation>onlySuccess());
                    }
                })
                .map(new Func1<UserLocation, BaseAdapterItem>() {
                    @Override
                    public BaseAdapterItem call(UserLocation userLocation) {
                        return new CurrentLocationAdapterItem(
                                userLocation, context.getString(R.string.location_header));
                    }
                });

        allAdapterItemsObservable = Observable.combineLatest(
                selectedLocationObservable.startWith((BaseAdapterItem) null),
                currentGpsLocationObservable.startWith((BaseAdapterItem) null),
                placesForQueryObservable,
                new Func3<BaseAdapterItem, BaseAdapterItem, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(BaseAdapterItem selectedLocation,
                                                      BaseAdapterItem gpsLocation,
                                                      List<BaseAdapterItem> queryLocations) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();
                        if (selectedLocation != null) {
                            builder.add(selectedLocation);
                        }
                        if (gpsLocation != null) {
                            builder.add(gpsLocation);
                        }

                        builder.addAll(queryLocations);

                        return builder.build();
                    }
                })
                .observeOn(uiScheduler);

        locationSelectedSubject
                .filter(Functions1.isNotNull())
                .switchMap(new Func1<String, Observable<PlaceBuffer>>() {
                    @Override
                    public Observable<PlaceBuffer> call(String placeId) {
                        final PendingResult<PlaceBuffer> result = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
                        final PlaceBuffer places = result.await(15, TimeUnit.SECONDS);

                        return Observable.just(places);
                    }
                })
                .subscribeOn(networkScheduler)
                .filter(new Func1<PlaceBuffer, Boolean>() {
                    @Override
                    public Boolean call(PlaceBuffer places) {
                        final boolean isSuccess = places.getStatus().isSuccess() && places.getCount() > 0;
                        if (!isSuccess) {
                            places.release();
                        }
                        return isSuccess;
                    }
                })
                .map(new Func1<PlaceBuffer, Place>() {
                    @Override
                    public Place call(PlaceBuffer places) {
                        return places.get(0);
                    }
                })
                .observeOn(uiScheduler);
    }


    @NonNull
    private Action1<Object> showProgressAction(final boolean showProgress) {
        return new Action1<Object>() {
            @Override
            public void call(Object ignore) {
                progressSubject.onNext(showProgress);
            }
        };
    }

    @Nonnull
    public Observer<String> getQuerySubject() {
        return querySubject;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressSubject.observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    @NonNull
    private Func1<String, Boolean> queryFilter() {
        return new Func1<String, Boolean>() {
            @Override
            public Boolean call(String query) {
                return googleApiClient.isConnected() &&
                        query != null &&
                        query.length() >= MINIMUM_SEARCH_INPUT;
            }
        };
    }
    private PendingResult<AutocompletePredictionBuffer> getResultsForQuery(String query) {
        final AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();

        return Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query,
                BOUNDS_WORLDWIDE, autocompleteFilter);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }

    @SuppressWarnings("MissingPermission")
    public void getCurrentLocation() {
        if (!googleApiClient.isConnected() && !PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            return;
        }
        android.location.Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        lastGpsLocationSubject.onNext(lastLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void disconnectGoogleApi() {
        googleApiClient.disconnect();
    }

    public static class PlaceAdapterItem implements BaseAdapterItem {
        @Nonnull
        private final String placeId;
        @Nonnull
        private final String fullText;
        @Nonnull
        private final Observer<String> locationSelectedSubject;

        private PlaceAdapterItem(@Nonnull String placeId,
                                 @Nonnull String fullText,
                                 @Nonnull Observer<String> locationSelectedSubject) {
            this.placeId = placeId;
            this.fullText = fullText;
            this.locationSelectedSubject = locationSelectedSubject;
        }

        @Nonnull
        public String getPlaceId() {
            return placeId;
        }

        @Nonnull
        public String getFullText() {
            return fullText;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        public void locationSelected() {
            locationSelectedSubject.onNext(placeId);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof PlaceAdapterItem && item.equals(this);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof PlaceAdapterItem && item.equals(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PlaceAdapterItem)) return false;
            final PlaceAdapterItem that = (PlaceAdapterItem) o;
            return Objects.equal(placeId, that.placeId) &&
                    Objects.equal(fullText, that.fullText);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(placeId, fullText);
        }
    }

    public static class CurrentLocationAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final UserLocation userLocation;
        @Nonnull
        private final String headerName;

        public CurrentLocationAdapterItem(@Nonnull UserLocation userLocation, @Nonnull String headerName) {
            this.userLocation = userLocation;
            this.headerName = headerName;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof CurrentLocationAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof CurrentLocationAdapterItem && this.equals(item);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CurrentLocationAdapterItem)) return false;
            final CurrentLocationAdapterItem that = (CurrentLocationAdapterItem) o;
            return Objects.equal(userLocation, that.userLocation) &&
                    Objects.equal(headerName, that.headerName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(userLocation, headerName);
        }

        @Nonnull
        public UserLocation getUserLocation() {
            return userLocation;
        }

        @Nonnull
        public String getHeaderName() {
            return headerName;
        }
    }
}
