package com.shoutit.app.android.view.location;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.api.model.UserLocation;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;

public class LocationForResultPresenter implements ILocationPresenter {

    private final LocationPresenterDelegate mLocationPresenterDelegate;
    private final Observable<UserLocation> mUpdateLocationObservable;

    @Inject
    public LocationForResultPresenter(LocationPresenterDelegate locationPresenterDelegate) {
        mLocationPresenterDelegate = locationPresenterDelegate;

        mUpdateLocationObservable = Observable.merge(
                mLocationPresenterDelegate.getSelectedPlaceGeocodeResponse().compose(ResponseOrError.<UserLocation>onlySuccess()),
                mLocationPresenterDelegate.getSelectedGpsUserLocation());
    }

    @Override
    @Nonnull
    public Observer<String> getQuerySubject() {
        return mLocationPresenterDelegate.getQuerySubject();
    }

    @Override
    @Nonnull
    public Observable<Boolean> getQueryProgressObservable() {
        return mLocationPresenterDelegate.getQueryProgressObservable();
    }

    @Override
    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return mLocationPresenterDelegate.getProgressObservable();
    }

    @Override
    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return mLocationPresenterDelegate.getAllAdapterItemsObservable();
    }

    @Override
    @Nonnull
    public Observable<Throwable> getLocationErrorObservable() {
        return mLocationPresenterDelegate.getLocationErrorObservable();
    }

    @Override
    @Nonnull
    public Observable<Throwable> getUpdateLocationErrorObservable() {
        return mLocationPresenterDelegate.getUpdateLocationErrorObservable();
    }

    @NonNull
    public Observable<UserLocation> getUpdateLocationObservable() {
        return mUpdateLocationObservable;
    }

    @Override
    public void refreshGpsLocation() {
        mLocationPresenterDelegate.refreshGpsLocation();
    }

    @Override
    public void disconnectGoogleApi() {
        mLocationPresenterDelegate.disconnectGoogleApi();
    }

    @NonNull
    @Override
    public Observable<Object> askForLocationPermissionsObservable() {
        return mLocationPresenterDelegate.askForLocationPermissionsObservable();
    }

    @NonNull
    @Override
    public Observable<Object> askForLocationEnableObservable() {
        return mLocationPresenterDelegate.getAskForLocationEnableObservable();
    }

    @Override
    public void locationSettingsChanged() {
        mLocationPresenterDelegate.refreshGpsLocation();
    }
}
