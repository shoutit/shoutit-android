package com.shoutit.app.android.dao;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CallerProfile;
import com.shoutit.app.android.api.model.UserIdentity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;

public class UsersIdentityDao {

    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final LoadingCache<String, UserIdentityDao> userIdentityCache;
    @Nonnull
    private final LoadingCache<String, UserByIdentityDao> userByIdentityCache;

    @Inject
    public UsersIdentityDao(@Nonnull final ApiService apiService,
                            @Nonnull final @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        userIdentityCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, UserIdentityDao>() {
                    @Override
                    public UserIdentityDao load(@Nonnull String username) throws Exception {
                        return new UserIdentityDao(username);
                    }
                });

        userByIdentityCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, UserByIdentityDao>() {
                    @Override
                    public UserByIdentityDao load(@Nonnull String identity) throws Exception {
                        return new UserByIdentityDao(identity);
                    }
                });
    }

    @Nonnull
    public Observable<ResponseOrError<UserIdentity>> getUserIdentityObservable(@Nonnull String username) {
        return userIdentityCache.getUnchecked(username).userIdentityObservable;
    }

    @Nonnull
    public Observable<ResponseOrError<CallerProfile>> getUserByIdentityObservable(@Nonnull String identity) {
        return userByIdentityCache.getUnchecked(identity).userByIdentityObservable;
    }

    public class UserIdentityDao {

        @Nonnull
        private final Observable<ResponseOrError<UserIdentity>> userIdentityObservable;

        public UserIdentityDao(@Nonnull String username) {

            userIdentityObservable = apiService.getUserIdentity(username)
                    .subscribeOn(networkScheduler)
                    .mergeWith(Observable.<UserIdentity>never())
                    .compose(ResponseOrError.<UserIdentity>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<UserIdentity>>cacheWithTimeout(networkScheduler));
        }
    }

    public class UserByIdentityDao {

        @Nonnull
        private final Observable<ResponseOrError<CallerProfile>> userByIdentityObservable;

        public UserByIdentityDao(@Nonnull String identity) {

            userByIdentityObservable = apiService.getUserByIdentity(identity)
                    .subscribeOn(networkScheduler)
                    .compose(ResponseOrError.<CallerProfile>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<CallerProfile>>cacheWithTimeout(networkScheduler))
                    .compose(ObservableExtensions.<ResponseOrError<CallerProfile>>behaviorRefCount());
        }
    }

}
