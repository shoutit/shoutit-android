package com.shoutit.app.android.view.profileslist;

import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.utils.ListeningHalfPresenter;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;

public abstract class ProfilesListPresenter {

    @Nonnull
    private final ListeningHalfPresenter listeningHalfPresenter;

    public ProfilesListPresenter(@Nonnull ListeningHalfPresenter listeningHalfPresenter) {
        this.listeningHalfPresenter = listeningHalfPresenter;
    }

    @NonNull
    abstract protected Observable<Boolean> getProgressObservable();

    @NonNull
    abstract protected Observable<Throwable> getErrorObservable();

    @NonNull
    abstract protected Observable<List<BaseAdapterItem>> getAdapterItemsObservable();

    @NonNull
    abstract protected Observable<String> getProfileToOpenObservable();

    abstract protected void refreshData();

    @NonNull
    abstract protected Observer<Object> getLoadMoreObserver();

    @Nonnull
    public Observable<String> getListenSuccessObservable() {
        return listeningHalfPresenter.getListenSuccess();
    }

    @Nonnull
    public Observable<String> getUnListenSuccessObservable() {
        return listeningHalfPresenter.getUnListenSuccess();
    }
}
