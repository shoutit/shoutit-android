package com.shoutit.app.android.view.profileslist;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;

public interface ProfilesListPresenter {

    @Nonnull
    Observable<String> getListenSuccessObservable();

    @Nonnull
    Observable<String> getUnListenSuccessObservable();

    Observable<Boolean> getProgressObservable();

    Observable<Throwable> getErrorObservable();

    Observable<List<BaseAdapterItem>> getAdapterItemsObservable();

    Observable<String> getProfileToOpenObservable();

    void refreshData();

    Observer<Object> getLoadMoreObserver();
}
