package com.shoutit.app.android.view.listenings;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.dao.ListeningsDao;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class ListeningsFragmentModule {

    @Nonnull
    private final ListeningsPresenter.ListeningsType listeningsType;

    public ListeningsFragmentModule(@Nonnull ListeningsPresenter.ListeningsType listeningsType) {
        this.listeningsType = listeningsType;
    }

    @Provides
    @FragmentScope
    ListeningsPresenter provideListeningsPresenter(@UiScheduler Scheduler uiScheduler,
                                                   ListeningsDao listeningsDao) {
        return new ListeningsPresenter(uiScheduler, listeningsDao, listeningsType);
    }
}

