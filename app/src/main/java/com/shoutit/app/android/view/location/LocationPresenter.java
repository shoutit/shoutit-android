package com.shoutit.app.android.view.location;

import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UpdateLocationRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;


public class LocationPresenter implements ILocationPresenter {

    @NonNull
    private final Observable<Object> userUpdateSuccessObservable;
    @Nonnull
    private final UserPreferences mUserPreferences;
    @NonNull
    private final LocationPresenterDelegate mLocationPresenterDelegate;

    @Inject
    public LocationPresenter(@Nonnull @NetworkScheduler final Scheduler networkScheduler,
                             @Nonnull @UiScheduler Scheduler uiScheduler,
                             @Nonnull final ApiService apiService,
                             @Nonnull final UserPreferences userPreferences,
                             @NonNull LocationPresenterDelegate locationPresenterDelegate) {
        mUserPreferences = userPreferences;
        mLocationPresenterDelegate = locationPresenterDelegate;

        final Observable<ResponseOrError<User>> updateUserObservable = Observable.merge(
                mLocationPresenterDelegate.getSelectedPlaceGeocodeResponse().compose(ResponseOrError.<UserLocation>onlySuccess()),
                mLocationPresenterDelegate.getSelectedGpsUserLocation())
                .switchMap(userLocation -> {
                    userPreferences.setAutomaticLocationTrackingEnabled(userLocation.isFromGps());

                    return apiService.updateUserLocation(new UpdateLocationRequest(userLocation))
                            .subscribeOn(networkScheduler)
                            .observeOn(uiScheduler)
                            .compose(ResponseOrError.<User>toResponseOrErrorObservable());
                })
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        userUpdateSuccessObservable = updateUserObservable
                .compose(ResponseOrError.<User>onlySuccess())
                .doOnNext(mLocationPresenterDelegate.showProgressAction(false))
                .doOnNext(saveToPreferencesAction())
                .observeOn(uiScheduler)
                .map(Functions1.toObject())
                .observeOn(uiScheduler);
    }

    @NonNull
    private Action1<User> saveToPreferencesAction() {
        return mUserPreferences::setUserOrPage;
    }

    @Nonnull
    public Observer<String> getQuerySubject() {
        return mLocationPresenterDelegate.getQuerySubject();
    }

    @Nonnull
    public Observable<Boolean> getQueryProgressObservable() {
        return mLocationPresenterDelegate.getQueryProgressObservable();
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return mLocationPresenterDelegate.getProgressObservable();
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return mLocationPresenterDelegate.getAllAdapterItemsObservable();
    }

    @Nonnull
    public Observable<Object> getUserUpdateSuccessObservable() {
        return userUpdateSuccessObservable;
    }

    @Nonnull
    public Observable<Throwable> getLocationErrorObservable() {
        return mLocationPresenterDelegate.getLocationErrorObservable();
    }

    @Nonnull
    public Observable<Throwable> getUpdateLocationErrorObservable() {
        return mLocationPresenterDelegate.getUpdateLocationErrorObservable();
    }

    public void refreshGpsLocation() {
        mLocationPresenterDelegate.refreshGpsLocation();
    }


    public void disconnectGoogleApi() {
        mLocationPresenterDelegate.disconnectGoogleApi();
    }
}
