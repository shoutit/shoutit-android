package com.shoutit.app.android.dao;

import com.appunite.rx.dagger.NetworkScheduler;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ProfilesListResponse;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class ListenersDaos {

    private final LoadingCache<String, ListenersDao> daoCache;

    public ListenersDaos(@Nonnull final ApiService apiService,
                         @Nonnull @NetworkScheduler final Scheduler networkScheduler) {

        daoCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ListenersDao>() {
                    @Override
                    public ListenersDao load(@Nonnull String userName) throws Exception {
                        return new ListenersDao(apiService, networkScheduler, userName);
                    }
                });
    }

    public ListenersDao getDao(@Nonnull String userName) {
        return daoCache.getUnchecked(userName);
    }

    public class ListenersDao extends BaseProfileListDao {

        private final ApiService apiService;

        public ListenersDao(final ApiService apiService,
                            @NetworkScheduler final Scheduler networkScheduler,
                            @Nonnull final String userName) {
            super(userName, networkScheduler);

            this.apiService = apiService;
        }

        @Override
        protected Observable<ProfilesListResponse> getRequest(int pageNumber) {
            return apiService.listeners(userName, pageNumber, PAGE_SIZE);
        }
    }
}
