package com.shoutit.app.android.view.listeningsandlisteners;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ListeningsDao;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class ListeningsActivityModule {

    @Provides
    @ActivityScope
    public ListeningsAndListenersPresenter providePresenter(ListeningsDao listeningsDao,
                                                            @UiScheduler Scheduler uiScheduler) {
        return new ListeningsPresenter(uiScheduler, listeningsDao);
    }
}
