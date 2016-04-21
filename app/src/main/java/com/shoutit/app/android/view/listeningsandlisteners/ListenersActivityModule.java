package com.shoutit.app.android.view.listeningsandlisteners;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ListenersDaos;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class ListenersActivityModule {

    @Provides
    @ActivityScope
    public ListenersPresenter providePresenter(@Nonnull String userName,
                                               ListenersDaos listenersDao,
                                               @UiScheduler Scheduler uiScheduler) {
        return new ListenersPresenter(userName, listenersDao, uiScheduler);
    }
}
