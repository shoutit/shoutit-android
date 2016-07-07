package com.shoutit.app.android.view.shouts_list_common;

import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;

public interface ShoutListPresenter {
    Observable<String> getShoutSelectedObservable();

    @Nonnull
    Observable<Throwable> getErrorObservable();

    @Nonnull
    Observable<List<BaseAdapterItem>> getAdapterItemsObservable();

    @Nonnull
    Observable<Boolean> getProgressObservable();

    @NonNull
    Observable<String> getBookmarkSuccessMessage();
}
