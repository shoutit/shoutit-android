package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class ProfilesDao {

    @Nonnull
    private final LoadingCache<String, ProfileDao> profilesCache;
    @Nonnull
    private final ApiService apiService;
    private final Scheduler networkScheduler;

    public ProfilesDao(@Nonnull ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        profilesCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ProfileDao>() {
                    @Override
                    public ProfileDao load(@Nonnull String userName) throws Exception {
                        return new ProfileDao(userName);
                    }
                });
    }

    @Nonnull
    public Observable<ResponseOrError<User>> getProfileObservable(@Nonnull String userName) {
        return profilesCache.getUnchecked(userName).getProfileObservable();
    }

    @Nonnull
    public Observer<Object> getRefreshProfileObserver(@Nonnull String userName) {
        return profilesCache.getUnchecked(userName).getRefreshSubject();
    }

    public class ProfileDao {
        @Nonnull
        private Observable<ResponseOrError<User>> profileObservable;
        @Nonnull
        private PublishSubject<Object> refreshSubject = PublishSubject.create();

        public ProfileDao(@Nonnull String userName) {
            profileObservable = apiService.getProfile(userName)
                    .subscribeOn(networkScheduler)
                    .compose(MoreOperators.<User>refresh(refreshSubject))
                    .compose(ResponseOrError.<User>toResponseOrErrorObservable())
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
    }
}
