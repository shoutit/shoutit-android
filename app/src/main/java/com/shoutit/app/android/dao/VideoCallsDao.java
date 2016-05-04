package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.TwilioResponse;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class VideoCallsDao {
    @Nonnull
    private Observable<ResponseOrError<TwilioResponse>> videoCallsObservable;

    public VideoCallsDao(@Nonnull final ApiService apiService,
                         @Nonnull @NetworkScheduler final Scheduler networkScheduler) {

        videoCallsObservable = apiService.getTokenAndIdentity()
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<TwilioResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<TwilioResponse>>cacheWithTimeout(networkScheduler));
    }

    @Nonnull
    public Observable<ResponseOrError<TwilioResponse>> getVideoCallsObservable() {
        return videoCallsObservable;
    }
}
