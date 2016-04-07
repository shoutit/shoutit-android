package com.shoutit.app.android.dao;


import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.constants.RequestsConstants;
import com.shoutit.app.android.model.FiltersToSubmit;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.model.MobilePhoneResponse;
import com.shoutit.app.android.model.RelatedShoutsPointer;
import com.shoutit.app.android.model.ReportBody;
import com.shoutit.app.android.model.SearchShoutPointer;
import com.shoutit.app.android.model.TagShoutsPointer;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.view.search.SearchPresenter;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ShoutsDao {

    private final static int PAGE_SIZE = 20;

    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final LoadingCache<LocationPointer, HomeShoutsDao> homeCache;
    @Nonnull
    private final LoadingCache<String, ShoutDao> shoutCache;
    @Nonnull
    private final LoadingCache<UserShoutsPointer, UserShoutsDao> userShoutsCache;
    @Nonnull
    private final LoadingCache<RelatedShoutsPointer, RelatedShoutsDao> relatedShoutsCache;
    @Nonnull
    private final LoadingCache<TagShoutsPointer, TagShoutsDao> tagsShoutsCache;
    @Nonnull
    private final LoadingCache<SearchShoutPointer, SearchShoutsDao> searchShoutCache;

    public ShoutsDao(@Nonnull final ApiService apiService,
                     @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                     @Nonnull final UserPreferences userPreferences) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;
        this.userPreferences = userPreferences;

        homeCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<LocationPointer, HomeShoutsDao>() {
                    @Override
                    public HomeShoutsDao load(@Nonnull LocationPointer locationPointer) throws Exception {
                        return new HomeShoutsDao(locationPointer);
                    }
                });

        shoutCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ShoutDao>() {
                    @Override
                    public ShoutDao load(@Nonnull String shoutId) throws Exception {
                        return new ShoutDao(shoutId);
                    }
                });

        userShoutsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<UserShoutsPointer, UserShoutsDao>() {
                    @Override
                    public UserShoutsDao load(@Nonnull UserShoutsPointer pointer) throws Exception {
                        return new UserShoutsDao(pointer);
                    }
                });

        relatedShoutsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<RelatedShoutsPointer, RelatedShoutsDao>() {
                    @Override
                    public RelatedShoutsDao load(@Nonnull RelatedShoutsPointer pointer) throws Exception {
                        return new RelatedShoutsDao(pointer);
                    }
                });

        tagsShoutsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<TagShoutsPointer, TagShoutsDao>() {
                    @Override
                    public TagShoutsDao load(@Nonnull TagShoutsPointer key) throws Exception {
                        return new TagShoutsDao(key);
                    }
                });

        searchShoutCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<SearchShoutPointer, SearchShoutsDao>() {
                    @Override
                    public SearchShoutsDao load(@Nonnull SearchShoutPointer key) throws Exception {
                        return new SearchShoutsDao(key);
                    }
                });
    }

    @Nonnull
    public Observable<ResponseOrError<ShoutsResponse>> getHomeShoutsObservable(@Nonnull LocationPointer locationPointer) {
        return homeCache.getUnchecked(locationPointer).getShoutsObservable();
    }

    @Nonnull
    public Observable<ResponseOrError<Shout>> getShoutObservable(@Nonnull String shoutId) {
        return shoutCache.getUnchecked(shoutId).getShoutObservable();
    }

    @Nonnull
    public ShoutDao getShoutDao(@Nonnull String shoutId) {
        return shoutCache.getUnchecked(shoutId);
    }

    @Nonnull
    public UserShoutsDao getUserShoutsDao(@Nonnull UserShoutsPointer pointer) {
        return userShoutsCache.getUnchecked(pointer);
    }

    @Nonnull
    public Observable<ResponseOrError<ShoutsResponse>> getUserShoutObservable(@Nonnull UserShoutsPointer pointer) {
        return userShoutsCache.getUnchecked(pointer).getShoutsObservable();
    }

    @Nonnull
    public Observable<ResponseOrError<ShoutsResponse>> getRelatedShoutsObservable(@Nonnull RelatedShoutsPointer pointer) {
        return relatedShoutsCache.getUnchecked(pointer).getShoutsObservable();
    }

    @Nonnull
    public Observable<ResponseOrError<ShoutsResponse>> getTagsShoutsObservable(@Nonnull TagShoutsPointer pointer) {
        return tagsShoutsCache.getUnchecked(pointer).getShoutsObservable();
    }

    @Nonnull
    public SearchShoutsDao getSearchShoutsDao(@Nonnull SearchShoutPointer pointer) {
        return searchShoutCache.getUnchecked(pointer);
    }

    @Nonnull
    public Observer<Object> getLoadMoreHomeShoutsObserver(LocationPointer locationPointer) {
        return homeCache.getUnchecked(locationPointer).getLoadMoreObserver();
    }

    @Nonnull
    public Observer<Object> getHomeShoutsRefreshObserver(LocationPointer pointer) {
        return homeCache.getUnchecked(pointer).getRefreshObserver();
    }

    @Nonnull
    public Observable<ResponseOrError<MobilePhoneResponse>> getShoutMobilePhoneObservable(@Nonnull String shoutId) {
        return shoutCache.getUnchecked(shoutId).getShoutMobileObservable();
    }

    @Nonnull
    public Observer<Object> getDeleteShoutObserver(@Nonnull String shoutId) {
        return shoutCache.getUnchecked(shoutId).getDeleteShoutObserver();
    }

    @Nonnull
    public Observable<Response<Object>> getDeleteShoutObservable(@Nonnull String shoutId) {
        return shoutCache.getUnchecked(shoutId).getDeleteShoutResponseObservable();
    }

    @Nonnull
    public Observer<String> getReportShoutObserver(String shoutId) {
        return shoutCache.getUnchecked(shoutId).getReportShoutObserver();
    }

    @Nonnull
    public Observable<Response<Object>> getReportShoutObservable(@Nonnull String shoutId) {
        return shoutCache.getUnchecked(shoutId).getReportShoutResponseObservable();
    }

    public class HomeShoutsDao extends BaseShoutsDao {

        @Nonnull
        private final LocationPointer mLocationPointer;

        public HomeShoutsDao(@Nonnull final LocationPointer locationPointer) {
            super(networkScheduler);
            mLocationPointer = locationPointer;
        }

        @Nonnull
        @Override
        Observable<ShoutsResponse> getShoutsRequest(int pageNumber) {
            if (userPreferences.isNormalUser()) {
                return apiService
                        .home(RequestsConstants.USER_ME, pageNumber, PAGE_SIZE)
                        .subscribeOn(networkScheduler);
            } else {
                return apiService
                        .shoutsForLocation(mLocationPointer.getCountryCode(),
                                mLocationPointer.getCity(), null, pageNumber, PAGE_SIZE,
                                null, null, null, null, null, null)
                        .subscribeOn(networkScheduler);
            }
        }
    }

    public class ShoutDao {
        @Nonnull
        private Observable<ResponseOrError<Shout>> shoutObservable;
        @Nonnull
        private Observable<ResponseOrError<MobilePhoneResponse>> shoutMobileObservable;
        @Nonnull
        private final PublishSubject<Object> deleteShoutObserver = PublishSubject.create();
        @Nonnull
        private final Observable<Response<Object>> deleteShoutResponseObservable;
        @Nonnull
        private final PublishSubject<String> reportShoutObserver = PublishSubject.create();
        @Nonnull
        private final Observable<Response<Object>> reportShoutResponseObservable;

        @Nonnull
        protected final PublishSubject<Object> refreshShoutsSubject = PublishSubject.create();

        public ShoutDao(@Nonnull final String shoutId) {
            final Observable<Object> refreshWithCache = Observable
                    .interval(2, TimeUnit.MINUTES, networkScheduler)
                    .map(Functions1.toObject());

            shoutObservable = apiService.shout(shoutId)
                    .subscribeOn(networkScheduler)
                    .compose(MoreOperators.<Shout>refresh(refreshShoutsSubject))
                    .compose(ResponseOrError.<Shout>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<Shout>>refresh(refreshWithCache))
                    .compose(MoreOperators.<ResponseOrError<Shout>>cacheWithTimeout(networkScheduler));

            shoutMobileObservable = apiService.shoutCall(shoutId)
                    .subscribeOn(networkScheduler)
                    .compose(MoreOperators.<MobilePhoneResponse>refresh(refreshShoutsSubject))
                    .compose(ResponseOrError.<MobilePhoneResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<MobilePhoneResponse>>refresh(refreshWithCache))
                    .compose(MoreOperators.<ResponseOrError<MobilePhoneResponse>>cacheWithTimeout(networkScheduler));

            deleteShoutResponseObservable = deleteShoutObserver
                    .flatMap(new Func1<Object, Observable<Response<Object>>>() {
                        @Override
                        public Observable<Response<Object>> call(Object o) {
                            return apiService.deleteShout(shoutId)
                                    .subscribeOn(networkScheduler);
                        }
                    });

            reportShoutResponseObservable = reportShoutObserver
                    .flatMap(new Func1<String, Observable<Response<Object>>>() {
                        @Override
                        public Observable<Response<Object>> call(String body) {
                            return apiService.report(ReportBody.forShout(shoutId, body))
                                    .subscribeOn(networkScheduler);
                        }
                    });

        }

        @Nonnull
        public Observable<ResponseOrError<Shout>> getShoutObservable() {
            return shoutObservable;
        }

        @Nonnull
        public Observer<Object> getRefreshObserver() {
            return refreshShoutsSubject;
        }

        @Nonnull
        public Observable<ResponseOrError<MobilePhoneResponse>> getShoutMobileObservable() {
            return shoutMobileObservable;
        }

        @Nonnull
        public Observer<Object> getDeleteShoutObserver() {
            return deleteShoutObserver;
        }

        @Nonnull
        public Observable<Response<Object>> getDeleteShoutResponseObservable() {
            return deleteShoutResponseObservable;
        }

        @Nonnull
        public Observable<Response<Object>> getReportShoutResponseObservable() {
            return reportShoutResponseObservable;
        }

        @Nonnull
        public PublishSubject<String> getReportShoutObserver() {
            return reportShoutObserver;
        }
    }


    public class UserShoutsDao extends BaseShoutsDao {
        @Nonnull
        private final UserShoutsPointer pointer;

        public UserShoutsDao(@Nonnull final UserShoutsPointer pointer) {
            super(networkScheduler);
            this.pointer = pointer;
        }

        @NonNull
        @Override
        Observable<ShoutsResponse> getShoutsRequest(int pageNumber) {
            return apiService
                    .shoutsForUser(pointer.getUserName(), pageNumber, pointer.getPageSize());
        }
    }


    public class RelatedShoutsDao extends BaseShoutsDao {
        @Nonnull
        private final RelatedShoutsPointer pointer;

        public RelatedShoutsDao(@Nonnull final RelatedShoutsPointer pointer) {
            super(networkScheduler);
            this.pointer = pointer;
        }

        @NonNull
        @Override
        Observable<ShoutsResponse> getShoutsRequest(int pageNumber) {
            return apiService
                    .shoutsRelated(pointer.getShoutId(), pageNumber, pointer.getPageSize());
        }
    }

    public class TagShoutsDao extends BaseShoutsDao {
        @Nonnull
        private final TagShoutsPointer pointer;

        public TagShoutsDao(@Nonnull final TagShoutsPointer pointer) {
            super(networkScheduler);
            this.pointer = pointer;
        }

        @NonNull
        @Override
        Observable<ShoutsResponse> getShoutsRequest(int pageNumber) {
            return apiService
                    .tagShouts(pointer.getTagName(), pageNumber, pointer.getPageSize());
        }
    }

    public class SearchShoutsDao extends BaseShoutsDao {
        @Nonnull
        private final SearchShoutPointer pointer;

        public SearchShoutsDao(@Nonnull final SearchShoutPointer pointer) {
            super(networkScheduler);
            this.pointer = pointer;
        }

        @NonNull
        @Override
        Observable<ShoutsResponse> getShoutsRequest(int pageNumber) {
            final SearchPresenter.SearchType searchType = pointer.getSearchType();
            final String query = pointer.getQuery();
            final String contextItemId = pointer.getContextItemId();
            final UserLocation location = pointer.getLocation();
            final FiltersToSubmit filtersToSubmit = pointer.getFiltersToSubmit();

            switch (SearchPresenter.SearchType.values()[searchType.ordinal()]) {
                case PROFILE:
                    return apiService.searchProfileShouts(query, pageNumber, PAGE_SIZE, contextItemId);
                case SHOUTS:
                    if (filtersToSubmit != null) {
                        return apiService.searchShouts(query, pageNumber, PAGE_SIZE,
                                location.getCountry(), filtersToSubmit.getCity(), filtersToSubmit.getState(),
                                filtersToSubmit.getMinPrice(), filtersToSubmit.getMaxPrice(),
                                filtersToSubmit.getDistance(), filtersToSubmit.getShoutType(),
                                filtersToSubmit.getSortType().getType(), filtersToSubmit.getFiltersQueryMap());
                    } else {
                        return apiService.searchShouts(query, pageNumber, PAGE_SIZE,
                                location.getCountry(), location.getCity(), location.getState(),
                                null, null, null, null, null, null);
                    }
                case RELATED_SHOUTS:
                    return apiService.shoutsRelated(contextItemId, pageNumber, PAGE_SIZE);
                case TAG:
                    if (filtersToSubmit != null) {
                        return apiService.searchTagShouts(query, pageNumber, PAGE_SIZE, contextItemId,
                                location.getCountry(), filtersToSubmit.getCity(), filtersToSubmit.getState(),
                                filtersToSubmit.getMinPrice(), filtersToSubmit.getMaxPrice(),
                                filtersToSubmit.getDistance(), filtersToSubmit.getShoutType(),
                                filtersToSubmit.getSortType().getType(), filtersToSubmit.getFiltersQueryMap());
                    } else {
                        return apiService.searchTagShouts(query, pageNumber, PAGE_SIZE, contextItemId,
                                location.getCountry(), location.getCity(), location.getState(),
                                null, null, null, null, null, null);
                    }
                case DISCOVER:
                    if (filtersToSubmit != null) {
                        return apiService.searchDiscoverShouts(query, pageNumber, PAGE_SIZE, contextItemId,
                                filtersToSubmit.getMinPrice(), filtersToSubmit.getMaxPrice(),
                                filtersToSubmit.getDistance(), filtersToSubmit.getShoutType(),
                                filtersToSubmit.getSortType().getType(), filtersToSubmit.getFiltersQueryMap());
                    } else {
                        return apiService.searchDiscoverShouts(query, pageNumber, PAGE_SIZE, contextItemId,
                                null, null, null, null, null, null);
                    }
                case BROWSE:
                    if (filtersToSubmit != null) {
                        return apiService.shoutsForLocation(location.getCountry(), filtersToSubmit.getCity(),
                                filtersToSubmit.getState(), pageNumber, PAGE_SIZE,
                                filtersToSubmit.getMinPrice(), filtersToSubmit.getMaxPrice(),
                                filtersToSubmit.getDistance(), filtersToSubmit.getShoutType(),
                                filtersToSubmit.getSortType().getType(), filtersToSubmit.getFiltersQueryMap());
                    } else {
                        return apiService.shoutsForLocation(location.getCountry(), location.getCity(),
                                location.getState(), pageNumber, PAGE_SIZE, null, null, null, null, null, null);
                    }
                default:
                    throw new RuntimeException("Unknwon profile type: " + SearchPresenter.SearchType.values()[searchType.ordinal()]);
            }
        }
    }
}
