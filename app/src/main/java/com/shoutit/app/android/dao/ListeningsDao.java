package com.shoutit.app.android.dao;

import com.appunite.rx.dagger.NetworkScheduler;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.model.ListeningsPointer;
import com.shoutit.app.android.view.listenings.ListeningsPresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class ListeningsDao {

    private final ApiService apiService;
    private final Scheduler networkScheduler;
    private final LoadingCache<ListeningsPointer, ListeningDao> daosCache;

    public ListeningsDao(final ApiService apiService,
                         @NetworkScheduler final Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        daosCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<ListeningsPointer, ListeningDao>() {
                    @Override
                    public ListeningDao load(@Nonnull ListeningsPointer key) throws Exception {
                        return new ListeningDao(key);
                    }
                });
    }

    @Nonnull
    public ListeningDao getDao(ListeningsPointer listeningsPointer) {
        return daosCache.getUnchecked(listeningsPointer);
    }

    public class ListeningDao extends BaseProfileListDao {

        private final ListeningsPresenter.ListeningsType listeningsType;
        private final ListeningsPointer pointer;

        public ListeningDao(ListeningsPointer pointer) {
            super(pointer.getUserName(), networkScheduler);
            this.pointer = pointer;

            this.listeningsType = pointer.getListeningsType();
        }

        @Nonnull
        public Observable<ProfilesListResponse> getRequest(int page) {
            switch (ListeningsPresenter.ListeningsType.values()[listeningsType.ordinal()]) {
                case USERS_AND_PAGES:
                    return apiService.profilesListenings(pointer.getUserName(), page, PAGE_SIZE);
                case INTERESTS:
                    return apiService.tagsListenings(pointer.getUserName(), page, PAGE_SIZE);
                default:
                    throw new RuntimeException("Unknown listening type");
            }
        }
    }
}
