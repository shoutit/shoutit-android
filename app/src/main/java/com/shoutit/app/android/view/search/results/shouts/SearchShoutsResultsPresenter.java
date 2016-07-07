package com.shoutit.app.android.view.search.results.shouts;

import android.content.Context;
import android.support.annotation.NonNull;

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
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BaseShoutsDao;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.model.FiltersToSubmit;
import com.shoutit.app.android.model.SearchShoutPointer;
import com.shoutit.app.android.utils.FBAdHalfPresenter;
import com.shoutit.app.android.utils.BookmarkHelper;
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
    @NonNull
    private final BookmarkHelper mBookmarkHelper;

    public SearchShoutsResultsPresenter(@Nonnull final ShoutsDao dao,
                                        @Nullable final String searchQuery,
                                        @Nonnull final SearchPresenter.SearchType searchType,
                                        @Nullable final String contextualItemId,
                                        @Nonnull final UserPreferences userPreferences,
                                        @Nonnull @ForActivity final Context context,
                                        @UiScheduler Scheduler uiScheduler,
                                        @Nonnull FBAdHalfPresenter fbAdHalfPresenter,
                                        @Nonnull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter,
                                        @NonNull BookmarksDao bookmarksDao,
                                        @NonNull BookmarkHelper bookmarkHelper) {
        mBookmarkHelper = bookmarkHelper;

        final boolean isNormalUser = userPreferences.isNormalUser();
        final BaseProfile currentUser = userPreferences.getUserOrPage();
        final String currentUserName = currentUser != null ? currentUser.getUsername() : null;

        final boolean initWithUserLocation = searchType != SearchPresenter.SearchType.PROFILE &&
                searchType != SearchPresenter.SearchType.TAG_PROFILE;

        final Observable<ShoutsDao.SearchShoutsDao> daoWithFilters = filtersSelectedSubject
                .map(filtersToSubmit -> dao.getSearchShoutsDao(new SearchShoutPointer(
                        searchQuery, searchType, contextualItemId, filtersToSubmit)));

        final Observable<ShoutsDao.SearchShoutsDao> daoObservable = Observable.just(initWithUserLocation)
                .flatMap(initWithUserLocation1 -> {
                    if (initWithUserLocation1) {
                        return userPreferences.getLocationObservable()
                                .filter(Functions1.isNotNull())
                                .first();
                    } else {
                        return Observable.just(null);
                    }
                })
                .map(userLocation -> dao.getSearchShoutsDao(new SearchShoutPointer(
                        searchQuery, searchType, userLocation, contextualItemId)))
                .mergeWith(daoWithFilters)
                .compose(ObservableExtensions.<ShoutsDao.SearchShoutsDao>behaviorRefCount());

        final Observable<ResponseOrError<ShoutsResponse>> shoutsRequest = daoObservable
                .switchMap(BaseShoutsDao::getShoutsObservable)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<ShoutsResponse> successShoutsResponse = shoutsRequest
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .compose(ObservableExtensions.<ShoutsResponse>behaviorRefCount());

        countObservable = successShoutsResponse
                .map(ShoutsResponse::getCount);

        final Observable<List<BaseAdapterItem>> shoutsItems = successShoutsResponse
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
                                    final BookmarkHelper.ShoutItemBookmarkHelper shoutItemBookmarkHelper = bookmarkHelper.getShoutItemBookmarkHelper();
                                    return new ShoutAdapterItem(shout, isShoutOwner, isNormalUser, context,
                                            shoutSelectedSubject, PromotionHelper.promotionInfoOrNull(shout),
                                            bookmarksDao.getBookmarkForShout(shout.getId(), shout.isBookmarked()),
                                            shoutItemBookmarkHelper.getObserver(), shoutItemBookmarkHelper.getEnableObservable());
                                }
                            }));
                            return builder.build();
                        }
                    }
                })
                .doOnNext(fbAdHalfPresenter::updatedShoutsCount);

        final PublishSubject<Boolean> isLinearLayoutSubject = PublishSubject.create();
        adapterItems = Observable.combineLatest(shoutsItems,
                fbAdHalfPresenter.getAdsObservable(isLinearLayoutSubject),
                FBAdHalfPresenter::combineShoutsWithAds);

        progressObservable = shoutsRequest.map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = shoutsRequest.compose(ResponseOrError.<ShoutsResponse>onlyError());

        loadMoreSubject
                .withLatestFrom(daoObservable, (Func2<Object, ShoutsDao.SearchShoutsDao, Observer<Object>>) (o, searchShoutsDao) -> searchShoutsDao.getLoadMoreObserver())
                .subscribe(loadMoreObserver -> {
                    loadMoreObserver.onNext(null);
                });

        shareClickedObservable = shareClickSubject.withLatestFrom(successShoutsResponse,
                (o, shoutsResponse) -> shoutsResponse.getWebUrl());

        refreshShoutsObservable = shoutsGlobalRefreshPresenter.getShoutsGlobalRefreshObservable()
                .withLatestFrom(daoObservable, (o, searchShoutsDao) -> {
                    searchShoutsDao.getRefreshObserver().onNext(null);
                    return null;
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

    @NonNull
    public Observable<String> getBookmarkSuccessMessage() {
        return mBookmarkHelper.getBookmarkSuccessMessage();
    }

    public void onShareClicked() {
        shareClickSubject.onNext(null);
    }

    public void setLinearLayoutManager(boolean isLinearLayour) {

    }
}
