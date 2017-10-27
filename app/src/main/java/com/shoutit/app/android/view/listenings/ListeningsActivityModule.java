package com.shoutit.app.android.view.listenings;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class ListeningsActivityModule {

    public ListeningsActivityModule() {
    }

    @Provides
    @ActivityScope
    BaseProfileListPresenter providesListeningsPresenter(@UiScheduler Scheduler uiScheduler,
                                                         ListeningsDao listeningsDao,
                                                         ListeningHalfPresenter listeningHalfPresenter,
                                                         UserPreferences userPreferences) {
        return new ListeningsPresenter(uiScheduler, listeningsDao, listeningHalfPresenter, userPreferences);
    }

}
