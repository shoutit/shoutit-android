package com.shoutit.app.android.dao;

import javax.annotation.Nonnull;
import rx.Observable;
import rx.subjects.PublishSubject;

public class ShoutsGlobalRefreshPresenter {

    @Nonnull
    public PublishSubject<Object> shoutsGlobalRefreshSubject = PublishSubject.create();

    public ShoutsGlobalRefreshPresenter() {
    }

    @Nonnull
    public Observable<Object> getShoutsGlobalRefreshObservable() {
        return shoutsGlobalRefreshSubject;
    }

    public void refreshShouts() {
        shoutsGlobalRefreshSubject.onNext(null);
    }
}
