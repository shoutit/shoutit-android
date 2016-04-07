package com.shoutit.app.android.dao;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.TwilioResponse;
import com.shoutit.app.android.api.model.UserIdentity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class VideoCallsDao {
    @Nonnull
    private Observable<ResponseOrError<TwilioResponse>> videoCallsObservable;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;

    @Inject
    public VideoCallsDao(@Nonnull final ApiService apiService,
                         @Nonnull @NetworkScheduler final Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        videoCallsObservable = apiService.getTokenAndIdentity()
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<TwilioResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<TwilioResponse>>cacheWithTimeout(networkScheduler))
                .compose(ObservableExtensions.<ResponseOrError<TwilioResponse>>behaviorRefCount());
    }

    @Nonnull
    public Observable<ResponseOrError<TwilioResponse>> getVideoCallsObservable() {
        return videoCallsObservable;
    }
}
