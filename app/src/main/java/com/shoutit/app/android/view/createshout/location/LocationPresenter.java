package com.shoutit.app.android.view.createshout.location;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.LocationUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class LocationPresenter {

    private static final long MINIMUM_SEARCH_INPUT = 3;

    @Nonnull
    private BehaviorSubject<String> querySubject = BehaviorSubject.create();
    @Nonnull
    private final PublishSubject<Object> gpsLocationRefreshSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<String> suggestedLocationSelectedSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<Boolean> queryProgressSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<Boolean> progressSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<UserLocation> selectedGpsUserLocation = PublishSubject.create();
    @Nonnull
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<Throwable> locationErrorObservable;
    @Nonnull
    private final Observable<Throwable> updateLocationErrorObservable;

    @Nonnull
    private final GoogleApiClient googleApiClient;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final ApiService apiService;
    private final Observable<UserLocation> mUpdateUserObservable;

    @Inject
    public LocationPresenter(@Nonnull final GoogleApiClient googleApiClient,
                             @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                             @Nonnull @UiScheduler Scheduler uiScheduler,
                             @Nonnull @ForApplication final Context context,
                             @Nonnull final ApiService apiService,
                             @Nonnull final UserPreferences userPreferences) {
        this.googleApiClient = googleApiClient;
        this.networkScheduler = networkScheduler;
        this.uiScheduler = uiScheduler;
        this.apiService = apiService;

        // Fetch gps location

        final Observable<Location> gpsLocationObservable = LocationUtils
                .getLocationObservable(googleApiClient, context, networkScheduler)
                .compose(MoreOperators.<Location>refresh(gpsLocationRefreshSubject))
                .filter(Functions1.isNotNull());

        // Currently selected manual location

        final Observable<BaseAdapterItem> currentlySelectedLocationObservable = userPreferences
                .getLocationObservable()
                .first()
                .filter(Functions1.isNotNull())
                .map((Func1<UserLocation, BaseAdapterItem>) userLocation -> new CurrentLocationAdapterItem(
                        userLocation, context.getString(R.string.location_header_selected_location),
                        false, selectedGpsUserLocation));


        // Current GPS location

        final Observable<BaseAdapterItem> currentGpsLocationObservable = gpsLocationObservable
                .filter(Functions1.isNotNull())
                .switchMap(location -> getGeoCodeRequest(location.getLatitude(), location.getLongitude())
                        .compose(ResponseOrError.<UserLocation>onlySuccess()))
                .map((Func1<UserLocation, BaseAdapterItem>) userLocation -> new CurrentLocationAdapterItem(
                        userLocation, context.getString(R.string.location_header),
                        true, selectedGpsUserLocation));


        // Locations suggestions from query

        final Observable<List<BaseAdapterItem>> placesForQueryObservable = querySubject
                .filter(queryFilter())
                .distinctUntilChanged()
                .doOnNext(showQueryProgressAction(true))
                .observeOn(networkScheduler)
                .switchMap(query -> {
                    final PendingResult<AutocompletePredictionBuffer> results =
                            LocationUtils.getPredictionsForQuery(googleApiClient, query);
                    final AutocompletePredictionBuffer predictions = results.await(15, TimeUnit.SECONDS);

                    return Observable.just(predictions);
                })
                .subscribeOn(networkScheduler)
                .doOnNext(showQueryProgressAction(false))
                .filter(predictions -> {
                    final boolean isSuccess = predictions.getStatus().isSuccess();
                    if (!isSuccess) {
                        predictions.release();
                    }
                    return isSuccess;
                })
                .map((Func1<AutocompletePredictionBuffer, List<BaseAdapterItem>>) predictions -> {

                    final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                    final Iterable<BaseAdapterItem> placesSuggestions = Iterables.transform(
                            predictions,
                            new Function<AutocompletePrediction, BaseAdapterItem>() {
                                @Nullable
                                @Override
                                public BaseAdapterItem apply(@Nullable AutocompletePrediction prediction) {
                                    assert prediction != null;
                                    return new PlaceAdapterItem(
                                            prediction.getPlaceId(),
                                            prediction.getFullText(null).toString(),
                                            suggestedLocationSelectedSubject);
                                }
                            });

                    builder.addAll(placesSuggestions);

                    // Release the buffer now that all data has been copied.
                    predictions.release();

                    return builder.build();
                })
                .observeOn(uiScheduler);


        // All locations displayed in adapter

        allAdapterItemsObservable = Observable.combineLatest(
                currentlySelectedLocationObservable.startWith((BaseAdapterItem) null),
                currentGpsLocationObservable.startWith((BaseAdapterItem) null),
                placesForQueryObservable.startWith(ImmutableList.<BaseAdapterItem>of()),
                (Func3<BaseAdapterItem, BaseAdapterItem, List<BaseAdapterItem>, List<BaseAdapterItem>>) (selectedLocation, gpsLocation, queryLocations) -> {
                    final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();
                    if (selectedLocation != null) {
                        builder.add(selectedLocation);
                    }
                    if (gpsLocation != null) {
                        builder.add(gpsLocation);
                    }

                    builder.addAll(queryLocations);

                    return builder.build();
                })
                .observeOn(uiScheduler);


        // Selected location action, update user

        final Observable<ResponseOrError<PlaceBuffer>> locationDetailsObservable = suggestedLocationSelectedSubject
                .filter(Functions1.isNotNull())
                .doOnNext(showProgressAction(true))
                .observeOn(networkScheduler)
                .switchMap(placeId -> {
                    final PendingResult<PlaceBuffer> result = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
                    final PlaceBuffer places = result.await(15, TimeUnit.SECONDS);

                    return Observable.just(places);
                })
                .subscribeOn(networkScheduler)
                .map(places -> places.getStatus().isSuccess() && places.getCount() > 0 ?
                        ResponseOrError.fromData(places) :
                        ResponseOrError.<PlaceBuffer>fromError(new Throwable(String.valueOf(places.getStatus().getStatusCode()))))
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<PlaceBuffer>>behaviorRefCount());

        final Observable<ResponseOrError<UserLocation>> selectedPlaceGeocodeResponse =
                locationDetailsObservable
                        .compose(ResponseOrError.<PlaceBuffer>onlySuccess())
                        .map(places -> places.get(0).getLatLng())
                        .switchMap(latLng -> {
                            return getGeoCodeRequest(latLng.latitude, latLng.longitude);  // TODO remove this request if guest user will be handled by API
                        })
                        .compose(ObservableExtensions.<ResponseOrError<UserLocation>>behaviorRefCount());

        mUpdateUserObservable = Observable.merge(
                selectedPlaceGeocodeResponse.compose(ResponseOrError.<UserLocation>onlySuccess()),
                selectedGpsUserLocation);

        // Progress and Errors

        progressObservable = Observable.merge(
                progressSubject,
                locationDetailsObservable.compose(ResponseOrError.<PlaceBuffer>onlyError()).map(Functions1.returnFalse()),
                selectedPlaceGeocodeResponse.compose(ResponseOrError.<UserLocation>onlyError()).map(Functions1.returnFalse())
                        .observeOn(uiScheduler));

        locationErrorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(ResponseOrError.transform(locationDetailsObservable)))
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        updateLocationErrorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(selectedPlaceGeocodeResponse)
                ))
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);
    }

    private Observable<ResponseOrError<UserLocation>> getGeoCodeRequest(double latitude, double longitude) {
        return apiService.geocode(LocationUtils.convertCoordinatesForRequest(latitude, longitude))
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ResponseOrError.<UserLocation>toResponseOrErrorObservable());
    }

    @NonNull
    private Action1<Object> showQueryProgressAction(final boolean showProgress) {
        return ignore -> queryProgressSubject.onNext(showProgress);
    }

    @NonNull
    private Action1<Object> showProgressAction(final boolean showProgress) {
        return ignore -> progressSubject.onNext(showProgress);
    }

    @Nonnull
    public Observer<String> getQuerySubject() {
        return querySubject;
    }

    @Nonnull
    public Observable<Boolean> getQueryProgressObservable() {
        return queryProgressSubject.observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    @Nonnull
    public Observable<Throwable> getLocationErrorObservable() {
        return locationErrorObservable;
    }

    @Nonnull
    public Observable<Throwable> getUpdateLocationErrorObservable() {
        return updateLocationErrorObservable;
    }

    @NonNull
    public Observable<UserLocation> getUpdateUserObservable() {
        return mUpdateUserObservable;
    }

    public void refreshGpsLocation() {
        gpsLocationRefreshSubject.onNext(null);
    }

    @Nonnull
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

    public void disconnectGoogleApi() {
        googleApiClient.disconnect();
    }

    // Adapter items

    public static class PlaceAdapterItem implements BaseAdapterItem {
        @Nonnull
        private final String placeId;
        @Nonnull
        private final String fullText;
        @Nonnull
        private final Observer<String> locationSelectedObserver;

        public PlaceAdapterItem(@Nonnull String placeId,
                                @Nonnull String fullText,
                                @Nonnull Observer<String> locationSelectedObserver) {
            this.placeId = placeId;
            this.fullText = fullText;
            this.locationSelectedObserver = locationSelectedObserver;
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
            locationSelectedObserver.onNext(placeId);
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
        private final boolean isGpsLocation;
        @Nonnull
        private final Observer<UserLocation> selectedGpsUserLocation;

        public CurrentLocationAdapterItem(@Nonnull UserLocation userLocation,
                                          @Nonnull String headerName,
                                          boolean isGpsLocation,
                                          @Nonnull Observer<UserLocation> selectedGpsUserLocation) {
            this.userLocation = userLocation;
            this.headerName = headerName;
            this.isGpsLocation = isGpsLocation;
            this.selectedGpsUserLocation = selectedGpsUserLocation;
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

        public void onLocationSelected() {
            if (isGpsLocation) {
                selectedGpsUserLocation.onNext(UserLocation.fromGps(userLocation));
            }
        }
    }
}
