package com.shoutit.app.android.view.home.myfeed;

import android.content.res.Resources;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseShoutAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.utils.FBAdHalfPresenter;
import com.shoutit.app.android.utils.PromotionHelper;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class MyFeedPresenter {

    private final PublishSubject<String> shoutSelectedObserver = PublishSubject.create();
    private final PublishSubject<Object> loadMoreShoutsSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> shoutsAdapterItems;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<Object> refreshShoutsObservable;
    private final Observable<Object> loadMoreObservable;

    @Inject
    public MyFeedPresenter(@Nonnull final ShoutsDao shoutsDao,
                           UserPreferences userPreferences,
                           FBAdHalfPresenter fbAdHalfPresenter,
                           @ForActivity Resources resources,
                           @UiScheduler Scheduler uiScheduler,
                           ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter) {

        final Observable<LocationPointer> locationObservable = userPreferences.getLocationObservable()
                .map(userLocation -> new LocationPointer(userLocation.getCountry(), userLocation.getCity(), userLocation.getState()))
                .compose(ObservableExtensions.<LocationPointer>behaviorRefCount());

        final Observable<ResponseOrError<ShoutsResponse>> shoutsRequestObservable = locationObservable
                .switchMap(shoutsDao::getHomeShoutsObservable)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        shoutsAdapterItems = shoutsRequestObservable
                .compose(ResponseOrError.onlySuccess())
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        if (!shoutsResponse.getShouts().isEmpty()) {
                            final Iterable<BaseAdapterItem> items = Iterables
                                    .transform(shoutsResponse.getShouts(), (Function<Shout, BaseAdapterItem>) shout -> {
                                        assert shout != null;
                                        return new BaseShoutAdapterItem(shout, resources, shoutSelectedObserver,
                                                PromotionHelper.promotionsInfoOrNull(shout));
                                    });

                            builder.addAll(items);
                        } else {
                            builder.add(new NoDataAdapterItem());
                        }

                        return builder.build();
                    }
                })
                .doOnNext(fbAdHalfPresenter::updatedShoutsCount);

        progressObservable = shoutsRequestObservable
                .map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = shoutsRequestObservable
                .compose(ResponseOrError.onlyError());

        refreshShoutsObservable = shoutsGlobalRefreshPresenter
                .getShoutsGlobalRefreshObservable()
                .withLatestFrom(locationObservable, (o, locationPointer) -> {
                    shoutsDao.getHomeShoutsRefreshObserver(locationPointer).onNext(null);
                    return null;
                });

        loadMoreObservable = loadMoreShoutsSubject
                .withLatestFrom(locationObservable, (o, locationPointer) -> {
                    shoutsDao.getLoadMoreHomeShoutsObserver(locationPointer).onNext(null);
                    return null;
                });

    }

    @Nonnull
    public Observable<Object> getRefreshShoutsObservable() {
        return refreshShoutsObservable;
    }

    public Observable<List<BaseAdapterItem>> getShoutsAdapterItems() {
        return shoutsAdapterItems;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedObserver;
    }

    @Nonnull
    public Observer<Object> getLoadMoreShouts() {
        return RxMoreObservers.ignoreCompleted(loadMoreShoutsSubject);
    }

    @Nonnull
    public Observable<Object> getLoadMoreObservable() {
        return loadMoreObservable;
    }
}
