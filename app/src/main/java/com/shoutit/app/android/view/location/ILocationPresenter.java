package com.shoutit.app.android.view.location;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;

public interface ILocationPresenter {
    @Nonnull
    Observer<String> getQuerySubject();

    @Nonnull
    Observable<Boolean> getQueryProgressObservable();

    @Nonnull
    Observable<Boolean> getProgressObservable();

    @Nonnull
    Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable();

    @Nonnull
    Observable<Throwable> getLocationErrorObservable();

    @Nonnull
    Observable<Throwable> getUpdateLocationErrorObservable();

    void refreshGpsLocation();

    void disconnectGoogleApi();
}
