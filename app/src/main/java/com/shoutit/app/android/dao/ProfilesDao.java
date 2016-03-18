package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.SearchProfileResponse;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class ProfilesDao {

    @Nonnull
    private final LoadingCache<String, ProfileDao> profilesCache;
    @Nonnull
    private final LoadingCache<String, SearchProfilesDao> searchProfilesCache;
    @Nonnull
    private final ApiService apiService;
    private final Scheduler networkScheduler;

    public ProfilesDao(@Nonnull ApiService apiService,
                       @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        profilesCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ProfileDao>() {
                    @Override
                    public ProfileDao load(@Nonnull String userName) throws Exception {
                        return new ProfileDao(userName);
                    }
                });

        searchProfilesCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, SearchProfilesDao>() {
                    @Override
                    public SearchProfilesDao load(@Nonnull String query) throws Exception {
                        return new SearchProfilesDao(query);
                    }
                });
    }

    @Nonnull
    public SearchProfilesDao getSearchProfilesDao(@Nonnull String query) {
        return searchProfilesCache.getUnchecked(query);
    }

    @Nonnull
    public Observable<ResponseOrError<User>> getProfileObservable(@Nonnull String userName) {
        return profilesCache.getUnchecked(userName).getProfileObservable();
    }

    @Nonnull
    public Observer<Object> getRefreshProfileObserver(@Nonnull String userName) {
        return profilesCache.getUnchecked(userName).getRefreshSubject();
    }

    @Nonnull
    public ProfileDao getProfileDao(@Nonnull String userName) {
        return profilesCache.getUnchecked(userName);
    }

    public class ProfileDao {
        @Nonnull
        private Observable<ResponseOrError<User>> profileObservable;
        @Nonnull
        private PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private PublishSubject<ResponseOrError<User>> updatedProfileLocallySubject = PublishSubject.create();

        public ProfileDao(@Nonnull final String userName) {
            profileObservable = apiService.getProfile(userName)
                    .subscribeOn(networkScheduler)
                    .compose(MoreOperators.<User>refresh(refreshSubject))
                    .compose(ResponseOrError.<User>toResponseOrErrorObservable())
                    .mergeWith(updatedProfileLocallySubject)
                    .compose(MoreOperators.<ResponseOrError<User>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<User>> getProfileObservable() {
            return profileObservable;
        }

        @Nonnull
        public PublishSubject<Object> getRefreshSubject() {
            return refreshSubject;
        }

        @Nonnull
        public Observer<ResponseOrError<User>> updatedProfileLocallyObserver() {
            return updatedProfileLocallySubject;
        }
    }

    public class SearchProfilesDao {
        private final int SEARCH_PAGE_SIZE = 20;

        @Nonnull
        private Observable<ResponseOrError<SearchProfileResponse>> profilesObservable;
        @Nonnull
        private PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private PublishSubject<ResponseOrError<SearchProfileResponse>> updatedProfilesLocallySubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<Object> loadMoreShoutsSubject = PublishSubject.create();

        public SearchProfilesDao(@Nonnull final String query) {
            final OperatorMergeNextToken<SearchProfileResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<SearchProfileResponse, Observable<SearchProfileResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<SearchProfileResponse> call(SearchProfileResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                if (previousResponse == null) {
                                    pageNumber = 0;
                                }
                                ++pageNumber;

                                final Observable<SearchProfileResponse> apiRequest = apiService
                                        .searchProfiles(query, pageNumber, SEARCH_PAGE_SIZE)
                                        .subscribeOn(networkScheduler);

                                if (previousResponse == null) {
                                    return apiRequest;
                                } else {
                                    return Observable.just(previousResponse).zipWith(apiRequest, new MergeSearchProfileResponses());
                                }
                            } else {
                                return Observable.never();
                            }
                        }
                    });


            profilesObservable = loadMoreShoutsSubject.startWith((Object) null)
                    .compose(MoreOperators.refresh(refreshSubject))
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<SearchProfileResponse>toResponseOrErrorObservable())
                    .mergeWith(updatedProfilesLocallySubject)
                    .compose(MoreOperators.<ResponseOrError<SearchProfileResponse>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<SearchProfileResponse>> getProfilesObservable() {
            return profilesObservable;
        }

        @Nonnull
        public PublishSubject<Object> getRefreshSubject() {
            return refreshSubject;
        }

        @Nonnull
        public Observer<ResponseOrError<SearchProfileResponse>> updatedProfileLocallyObserver() {
            return updatedProfilesLocallySubject;
        }
    }

    @Nonnull
    public Observable<User> updateUser() {
        return apiService.getMyUser()
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<User>toResponseOrErrorObservable())
                .compose(ResponseOrError.<User>onlySuccess());
    }

    public class MergeSearchProfileResponses implements Func2<SearchProfileResponse, SearchProfileResponse, SearchProfileResponse> {
        @Override
        public SearchProfileResponse call(SearchProfileResponse previousData, SearchProfileResponse newData) {
            final ImmutableList<User> allItems = ImmutableList.<User>builder()
                    .addAll(previousData.getResults())
                    .addAll(newData.getResults())
                    .build();

            final int count = previousData.getCount() + newData.getCount();
            return new SearchProfileResponse(count, newData.getNext(), newData.getPrevious(), allItems);
        }
    }
}
