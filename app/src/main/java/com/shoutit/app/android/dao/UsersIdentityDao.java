package com.shoutit.app.android.dao;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UserIdentity;

import java.util.concurrent.TimeUnit;

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
    }

    @Nonnull
    public Observable<ResponseOrError<UserIdentity>> getUserIdentityObservable(@Nonnull String username) {
        return userIdentityCache.getUnchecked(username).userIdentityObservable;
    }

    public class UserIdentityDao {

        @Nonnull
        private final Observable<ResponseOrError<UserIdentity>> userIdentityObservable;

        public UserIdentityDao(@Nonnull String username) {

            final Observable<Object> refreshWithCache = Observable
                    .interval(1, TimeUnit.MINUTES, networkScheduler)
                    .map(Functions1.toObject());

            userIdentityObservable = apiService.getUserIdentity(username)
                    .subscribeOn(networkScheduler)
                    .compose(ResponseOrError.<UserIdentity>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<UserIdentity>>refresh(refreshWithCache))
                    .compose(MoreOperators.<ResponseOrError<UserIdentity>>cacheWithTimeout(networkScheduler))
                    .compose(ObservableExtensions.<ResponseOrError<UserIdentity>>behaviorRefCount());
        }

        @Nonnull
        public Observable<ResponseOrError<UserIdentity>> getUserIdentityObservable() {
            return userIdentityObservable;
        }
    }

}
