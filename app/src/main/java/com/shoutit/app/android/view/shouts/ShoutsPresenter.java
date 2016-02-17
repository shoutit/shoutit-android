package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.DiscoverShoutsDao;

import java.util.List;

import javax.annotation.Nullable;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class ShoutsPresenter {

    private final Observable<List<ShoutAdapterItem>> mListObservable;
    private final Observable<Throwable> mThrowableObservable;

    public ShoutsPresenter(@NetworkScheduler Scheduler networkScheduler,
                           @UiScheduler Scheduler uiScheduler,
                           DiscoverShoutsDao discoverShoutsDao,
                           String discoverId,
                           @ForActivity final Context context) {
        final Observable<ResponseOrError<ShoutsResponse>> observable = discoverShoutsDao.getShoutsObservable(discoverId)
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler);

        mListObservable = observable.compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<ShoutAdapterItem>>() {
                    @Override
                    public List<ShoutAdapterItem> call(ShoutsResponse shoutsResponse) {
                        return ImmutableList.copyOf(Iterables.transform(shoutsResponse.getShouts(), new Function<Shout, ShoutAdapterItem>() {
                            @Nullable
                            @Override
                            public ShoutAdapterItem apply(Shout input) {
                                return new ShoutAdapterItem(input, context);
                            }
                        }));
                    }
                });

        mThrowableObservable = observable.compose(ResponseOrError.<ShoutsResponse>onlyError());
        Observable.merge(mListObservable)
    }

    @NonNull
    public Observable<List<ShoutAdapterItem>> getSuccessObservable() {
        return mListObservable;
    }

    @NonNull
    public Observable<Throwable> getFailObservable() {
        return mThrowableObservable;
    }

    @NonNull
    public Observable<Boolean> getProgressVisible() {
        return null;
    }
}