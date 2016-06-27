package com.shoutit.app.android.dao;

import com.appunite.rx.dagger.NetworkScheduler;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.view.listenings.ListeningsPresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class ListeningsDao {

    private final ApiService apiService;
    private final Scheduler networkScheduler;
    private final LoadingCache<ListeningsPresenter.ListeningsType, ListeningDao> daosCache;

    public ListeningsDao(final ApiService apiService,
                         @NetworkScheduler final Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        daosCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<ListeningsPresenter.ListeningsType, ListeningDao>() {
                    @Override
                    public ListeningDao load(@Nonnull ListeningsPresenter.ListeningsType key) throws Exception {
                        return new ListeningDao(key);
                    }
                });
    }

    @Nonnull
    public ListeningDao getDao(ListeningsPresenter.ListeningsType listeningsType) {
        return daosCache.getUnchecked(listeningsType);
    }

    public class ListeningDao extends BaseProfileListDao {

        private final ListeningsPresenter.ListeningsType listeningsType;

        public ListeningDao(ListeningsPresenter.ListeningsType listeningsType) {
            super(User.ME, networkScheduler);

            this.listeningsType = listeningsType;
        }

        @Nonnull
        public Observable<ProfilesListResponse> getRequest(int page) {
            switch (ListeningsPresenter.ListeningsType.values()[listeningsType.ordinal()]) {
                case USERS_AND_PAGES:
                    return apiService.profilesListenings(page, PAGE_SIZE);
                case INTERESTS:
                    return apiService.tagsListenings(page, PAGE_SIZE);
                default:
                    throw new RuntimeException("Unknown listening type");
            }
        }
    }

}
