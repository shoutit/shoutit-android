package com.shoutit.app.android.view.location;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.UserLocation;

import javax.annotation.Nonnull;

import rx.Observer;

public class CurrentLocationAutomaticUpdatesAdapterItem extends BaseNoIDAdapterItem {


    @Nonnull
    private final LocationPresenterDelegate.UserLocationWithInfo userLocationWithInfo;
    @Nonnull
    private final String headerName;
    @Nonnull
    private final Observer<UserLocation> selectedGpsUserLocation;
    @Nonnull
    private final Observer<Object> askForLocationPermissionsSubject;
    @Nonnull
    private final Observer<Object> askForLocationEnableObserver;

    public CurrentLocationAutomaticUpdatesAdapterItem(@Nonnull LocationPresenterDelegate.UserLocationWithInfo userLocationWithInfo,
                                                      @Nonnull String headerName,
                                                      @Nonnull Observer<UserLocation> selectedGpsUserLocation,
                                                      @Nonnull Observer<Object> askForLocationPermissionsSubject,
                                                      @Nonnull Observer<Object> askForLocationEnableObserver) {
        this.userLocationWithInfo = userLocationWithInfo;
        this.headerName = headerName;
        this.selectedGpsUserLocation = selectedGpsUserLocation;
        this.askForLocationPermissionsSubject = askForLocationPermissionsSubject;
        this.askForLocationEnableObserver = askForLocationEnableObserver;
    }

    @Override
    public long adapterId() {
        return BaseAdapterItem.NO_ID;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof CurrentLocationAutomaticUpdatesAdapterItem;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof CurrentLocationAutomaticUpdatesAdapterItem && this.equals(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrentLocationAutomaticUpdatesAdapterItem)) return false;
        final CurrentLocationAutomaticUpdatesAdapterItem that = (CurrentLocationAutomaticUpdatesAdapterItem) o;
        return Objects.equal(userLocationWithInfo, that.userLocationWithInfo) &&
                Objects.equal(headerName, that.headerName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userLocationWithInfo, headerName);
    }

    @Nonnull
    public Optional<UserLocation> getOptionalUserLocation() {
        return userLocationWithInfo.getUserLocation();
    }

    public boolean hasLocationPermissions() {
        return userLocationWithInfo.getLocationInfo().isHasPermissions();
    }

    @Nonnull
    public String getHeaderName() {
        return headerName;
    }

    public void onLocationSelected() {
        if (!userLocationWithInfo.getLocationInfo().isHasPermissions()) {
            askForLocationPermissionsSubject.onNext(null);
        } else if (!userLocationWithInfo.getLocationInfo().isLocationServiceEnabled()) {
            askForLocationEnableObserver.onNext(null);
        } else {
            selectedGpsUserLocation.onNext(UserLocation.fromGps(userLocationWithInfo.getUserLocation().get()));
        }
    }
}
