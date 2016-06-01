package com.shoutit.app.android.view.listenings;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class ListeningsActivityModule {

    private final ListeningsPresenter.ListeningsType listeningsType;

    public ListeningsActivityModule(ListeningsPresenter.ListeningsType listeningsType) {
        this.listeningsType = listeningsType;
    }

    @Provides
    @ActivityScope
    ProfilesListPresenter providesListeningsPresenter(@UiScheduler Scheduler uiScheduler,
                                                      ListeningsDao listeningsDao,
                                                      @NetworkScheduler Scheduler networkScheduler,
                                                      ApiService apiService) {
        return new ListeningsPresenter(uiScheduler, listeningsDao, listeningsType,
                networkScheduler, apiService);
    }

}
