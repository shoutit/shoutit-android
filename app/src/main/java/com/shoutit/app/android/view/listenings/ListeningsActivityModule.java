package com.shoutit.app.android.view.listenings;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.ListeningsDao;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class ListeningsActivityModule {

    private final ListeningsPresenter.ListeningsType listeningsType;

    public ListeningsActivityModule(@Nonnull ListeningsActivity activity,
                                    ListeningsPresenter.ListeningsType listeningsType) {
        this.listeningsType = listeningsType;
    }

    @Provides
    ListeningsPresenter providesListeningsPresenter(@UiScheduler Scheduler uiScheduler,
                                                    ListeningsDao listeningsDao,
                                                    @NetworkScheduler Scheduler networkScheduler,
                                                    ApiService apiService) {
        return new ListeningsPresenter(uiScheduler, listeningsDao, listeningsType,
                networkScheduler, apiService);
    }

}
