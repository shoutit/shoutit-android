package com.shoutit.app.android.view.listenings;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

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
    ListenUserOrPageHalfPresenter listeningHalfPresenter(ApiService apiService,
                                                  @NetworkScheduler Scheduler networkScheduler,
                                                  @UiScheduler Scheduler uiScheduler) {
        return new ListenUserOrPageHalfPresenter(apiService, networkScheduler,
                uiScheduler, listeningsType);
    }

    @Provides
    @ActivityScope
    BaseProfileListPresenter providesListeningsPresenter(@UiScheduler Scheduler uiScheduler,
                                                         ListeningsDao listeningsDao,
                                                         ListenUserOrPageHalfPresenter listeningHalfPresenter,
                                                         UserPreferences userPreferences) {
        return new ListeningsPresenter(uiScheduler, listeningsDao, listeningsType,
                listeningHalfPresenter, userPreferences);
    }

}
