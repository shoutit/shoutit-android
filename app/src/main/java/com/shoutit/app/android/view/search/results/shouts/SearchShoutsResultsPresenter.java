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
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.model.FiltersToSubmit;
import com.shoutit.app.android.model.SearchShoutPointer;
import com.shoutit.app.android.utils.PromotionHelper;
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
    private final PublishSubject<Object> shareClickSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> adapterItems;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<Integer> countObservable;
    private final Observable<String> shareClickedObservable;
    private final Observable<Object> refreshShoutsObservable;

    public SearchShoutsResultsPresenter(@Nonnull final ShoutsDao dao,
                                        @Nullable final String searchQuery,
                                        @Nonnull final SearchPresenter.SearchType searchType,
                                        @Nullable final String contextualItemId,
                                        @Nonnull final UserPreferences userPreferences,
                                        @Nonnull @ForActivity final Context context,
                                        @UiScheduler Scheduler uiScheduler,
                                        @Nonnull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter) {

        final boolean isNormalUser = userPreferences.isNormalUser();
        final BaseProfile currentUser = userPreferences.getPageOrUser();
        final String currentUserName = currentUser != null ? currentUser.getUsername() : null;

        final boolean initWithUserLocation = searchType != SearchPresenter.SearchType.PROFILE &&
                searchType != SearchPresenter.SearchType.TAG_PROFILE;

        final Observable<ShoutsDao.SearchShoutsDao> daoWithFilters = filtersSelectedSubject
                .map(new Func1<FiltersToSubmit, ShoutsDao.SearchShoutsDao>() {
                    @Override
                    public ShoutsDao.SearchShoutsDao call(FiltersToSubmit filtersToSubmit) {
                        return dao.getSearchShoutsDao(new SearchShoutPointer(
                                searchQuery, searchType, contextualItemId, filtersToSubmit));
                    }
                });

        final Observable<ShoutsDao.SearchShoutsDao> daoObservable = Observable.just(initWithUserLocation)
                .flatMap(new Func1<Boolean, Observable<UserLocation>>() {
                    @Override
                    public Observable<UserLocation> call(Boolean initWithUserLocation) {
                        if (initWithUserLocation) {
                            return userPreferences.getLocationObservable()
                                    .filter(Functions1.isNotNull())
                                    .first();
                        } else {
                            return Observable.just(null);
                        }
                    }
                })
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

        final Observable<ShoutsResponse> successShoutsResponse = shoutsRequest
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .compose(ObservableExtensions.<ShoutsResponse>behaviorRefCount());

        countObservable = successShoutsResponse
                .map(new Func1<ShoutsResponse, Integer>() {
                    @Override
                    public Integer call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getCount();
                    }
                });

        adapterItems = successShoutsResponse
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        if (shoutsResponse.getShouts().isEmpty()) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataTextAdapterItem(context.getString(R.string.search_results_no_results)));
                        } else {
                            final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();
                            builder.addAll(Lists.transform(shoutsResponse.getShouts(), new Function<Shout, BaseAdapterItem>() {
                                @Nullable
                                @Override
                                public BaseAdapterItem apply(Shout shout) {
                                    final boolean isShoutOwner = shout.getProfile().getUsername().equals(currentUserName);
                                    return new ShoutAdapterItem(shout, isShoutOwner, isNormalUser, context, shoutSelectedSubject, PromotionHelper.promotionInfoOrNull(shout));
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

        shareClickedObservable = shareClickSubject.withLatestFrom(successShoutsResponse,
                new Func2<Object, ShoutsResponse, String>() {
                    @Override
                    public String call(Object o, ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getWebUrl();
                    }
                });

        refreshShoutsObservable = shoutsGlobalRefreshPresenter.getShoutsGlobalRefreshObservable()
                .withLatestFrom(daoObservable, new Func2<Object, ShoutsDao.SearchShoutsDao, Object>() {
                    @Override
                    public Object call(Object o, ShoutsDao.SearchShoutsDao searchShoutsDao) {
                        searchShoutsDao.getRefreshObserver().onNext(null);
                        return null;
                    }
                });
    }

    public Observable<Object> getRefreshShoutsObservable() {
        return refreshShoutsObservable;
    }

    public Observable<String> getShareClickedObservable() {
        return shareClickedObservable;
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

    public void onShareClicked() {
        shareClickSubject.onNext(null);
    }
}
