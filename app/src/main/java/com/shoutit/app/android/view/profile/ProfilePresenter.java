package com.shoutit.app.android.view.profile;


import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;

public interface ProfilePresenter {

    @NonNull
    @Nonnull
    Observable<Boolean> getProgressObservable();

    @NonNull
    @Nonnull
    Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable();

    @Nonnull
    Observable<Object> getActionOnlyForLoggedInUserObservable();

    @NonNull
    @Nonnull
    Observable<Throwable> getErrorObservable();

    @Nonnull
    Observable<String> getAvatarObservable();

    @Nonnull
    Observable<String> getCoverUrlObservable();

    @Nonnull
    Observable<String> getToolbarTitleObservable();

    @Nonnull
    Observable<String> getToolbarSubtitleObservable();

    @Nonnull
    Observable<String> getShareObservable();

    @Nonnull
    Observable<String> getShoutSelectedObservable();

    @Nonnull
    Observable<String> getProfileToOpenObservable();

    @Nonnull
    Observer<Object> getShareInitObserver();

    @Nonnull
    Observable<Object> getMoreMenuOptionClickedSubject();

    @Nonnull
    Observable<String> getSeeAllShoutsObservable();

    void refreshProfile();

    @Nonnull
    Observer<String> sendReportObserver();
}
