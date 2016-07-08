package com.shoutit.app.android.view.location;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.api.model.UserLocation;

import javax.annotation.Nonnull;

import rx.Observer;

public class CurrentLocationAdapterItem implements BaseAdapterItem {

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