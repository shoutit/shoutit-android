package com.shoutit.app.android.view.search.results.shouts;

import android.content.Context;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.model.FiltersToSubmit;
import com.shoutit.app.android.model.SearchShoutPointer;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class SearchShoutsResultsPresenter {

    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
    private final PublishSubject<FiltersToSubmit> filtersSelectedSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> adapterItems;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<Integer> countObservable;

    public SearchShoutsResultsPresenter(@Nonnull final ShoutsDao dao,
                                        @Nullable final String searchQuery,
                                        @Nonnull final SearchPresenter.SearchType searchType,
                                        @Nullable final String contextualItemId,
                                        @Nonnull UserPreferences userPreferences,
                                        @Nonnull @ForActivity final Context context,
                                        @UiScheduler Scheduler uiScheduler) {

        final Observable<ShoutsDao.SearchShoutsDao> daoWithFilters = filtersSelectedSubject
                .map(new Func1<FiltersToSubmit, ShoutsDao.SearchShoutsDao>() {
                    @Override
                    public ShoutsDao.SearchShoutsDao call(FiltersToSubmit filtersToSubmit) {
                        return dao.getSearchShoutsDao(new SearchShoutPointer(
                                searchQuery, searchType, contextualItemId, filtersToSubmit));
                    }
                });

        final Observable<ShoutsDao.SearchShoutsDao> daoObservable = userPreferences.getLocationObservable()
                .filter(Functions1.isNotNull())
                .first()
                .distinctUntilChanged()
                .map(new Func1<UserLocation, ShoutsDao.SearchShoutsDao>() {
                    @Override
                    public ShoutsDao.SearchShoutsDao call(UserLocation userLocation) {
                        return dao.getSearchShoutsDao(new SearchShoutPointer(
                                searchQuery, searchType, userLocation, contextualItemId));
                    }
                })
                .mergeWith(daoWithFilters)
                .compose(ObservableExtensions.<ShoutsDao.SearchShoutsDao>behaviorRefCount());

        final Observable<ResponseOrError<ShoutsResponse>> shoutsRequest = daoObservable
                .switchMap(new Func1<ShoutsDao.SearchShoutsDao, Observable<ResponseOrError<ShoutsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ShoutsResponse>> call(ShoutsDao.SearchShoutsDao searchShoutsDao) {
                        return searchShoutsDao.getShoutsObservable();
                    }
                })
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        countObservable = shoutsRequest
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, Integer>() {
                    @Override
                    public Integer call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getCount();
                    }
                });

        adapterItems = shoutsRequest
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        if (shoutsResponse.getShouts().isEmpty()) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataAdapterItem());
                        } else {
                            final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();
                            builder.addAll(Lists.transform(shoutsResponse.getShouts(), new Function<Shout, BaseAdapterItem>() {
                                @Nullable
                                @Override
                                public BaseAdapterItem apply(Shout input) {
                                    return new ShoutAdapterItem(input, context, shoutSelectedSubject);
                                }
                            }));
                            return builder.build();
                        }
                    }
                });

        progressObservable = shoutsRequest.map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = shoutsRequest.compose(ResponseOrError.<ShoutsResponse>onlyError());

        loadMoreSubject
                .withLatestFrom(daoObservable, new Func2<Object, ShoutsDao.SearchShoutsDao, Observer<Object>>() {
                    @Override
                    public Observer<Object> call(Object o, ShoutsDao.SearchShoutsDao searchShoutsDao) {
                        return searchShoutsDao.getLoadMoreObserver();
                    }
                })
                .subscribe(new Action1<Observer<Object>>() {
                    @Override
                    public void call(Observer<Object> loadMoreObserver) {
                        loadMoreObserver.onNext(null);
                    }
                });
    }

    public Observable<List<BaseAdapterItem>> getAdapterItems() {
        return adapterItems;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedSubject;
    }

    public Observer<Object> getLoadMoreObserver() {
        return loadMoreSubject;
    }

    public Observer<FiltersToSubmit> getFiltersSelectedObserver() {
        return filtersSelectedSubject;
    }

    public Observable<Integer> getCountObservable() {
        return countObservable;
    }
}
