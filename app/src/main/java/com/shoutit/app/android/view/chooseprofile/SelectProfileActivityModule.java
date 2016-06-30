package com.shoutit.app.android.view.chooseprofile;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.dao.ListeningsDao;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class SelectProfileActivityModule {

    @Provides
    @ActivityScope
    SelectProfilePresenter providesSelectProfilePresenter(ListeningsDao listeningsDao,
                                                          ListenersDaos listenersDao,
                                                          @UiScheduler Scheduler uiScheduler,
                                                          UserPreferences userPreferences) {
        return new SelectProfilePresenter(listeningsDao, listenersDao, uiScheduler, userPreferences);
    }
}
