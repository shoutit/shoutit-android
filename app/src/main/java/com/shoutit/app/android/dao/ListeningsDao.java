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

public class ListeningsDao {

    private final ApiService apiService;
    private final Scheduler networkScheduler;
    private final LoadingCache<String, ListeningDao> daosCache;

    public ListeningsDao(final ApiService apiService,
                         @NetworkScheduler final Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        daosCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ListeningDao>() {
                    @Override
                    public ListeningDao load(@Nonnull String userName) throws Exception {
                        return new ListeningDao(userName);
                    }
                });
    }

    @Nonnull
    public ListeningDao getDao(String userName) {
        return daosCache.getUnchecked(userName);
    }

    public class ListeningDao extends BaseProfileListDao {

        public ListeningDao(String userName) {
            super(userName, networkScheduler);
        }

        @Nonnull
        public Observable<ProfilesListResponse> getRequest(int page) {
            return apiService.profilesListenings(userName, page, PAGE_SIZE);
        }
    }
}
