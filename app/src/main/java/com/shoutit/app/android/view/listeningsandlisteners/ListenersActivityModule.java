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

    @Nonnull
    private final String userName;

    public ListenersActivityModule(@Nonnull String userName) {
        this.userName = userName;
    }

    @Provides
    @ActivityScope
    public ListeningsAndListenersPresenter providePresenter(ListenersDaos listenersDao,
                                               @UiScheduler Scheduler uiScheduler) {
        return new ListenersPresenter(userName, listenersDao, uiScheduler);
    }
}
