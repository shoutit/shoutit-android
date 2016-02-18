package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;

public class DiscoverShoutsPresenter {

    private final Observable<List<BaseAdapterItem>> mListObservable;
    private final Observable<Throwable> mThrowableObservable;
    private final Observable<Boolean> mProgressObservable;
    private final DiscoverShoutsDao mDiscoverShoutsDao;

    @Inject
    public DiscoverShoutsPresenter(@NetworkScheduler Scheduler networkScheduler,
                                   @UiScheduler Scheduler uiScheduler,
                                   DiscoverShoutsDao discoverShoutsDao,
                                   String discoverId,
                                   @ForActivity final Context context) {
        mDiscoverShoutsDao = discoverShoutsDao;
        final Observable<ResponseOrError<ShoutsResponse>> observable = discoverShoutsDao.getShoutsObservable(discoverId)
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler);

        mListObservable = observable.compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        return ImmutableList.copyOf(Iterables.transform(shoutsResponse.getShouts(), new Function<Shout, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(Shout input) {
                                return new DiscoverShoutAdapterItem(input, context);
                            }
                        }));
                    }
                });

        mThrowableObservable = observable.compose(ResponseOrError.<ShoutsResponse>onlyError());
        mProgressObservable = observable.map(Functions1.returnFalse()).startWith(true);
    }

    @NonNull
    public Observable<List<BaseAdapterItem>> getSuccessObservable() {
        return mListObservable;
    }

    @NonNull
    public Observable<Throwable> getFailObservable() {
        return mThrowableObservable;
    }

    @NonNull
    public Observable<Boolean> getProgressVisible() {
        return mProgressObservable;
    }

    @NonNull
    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(mDiscoverShoutsDao.getLoadMoreShoutsSubject());
    }
}