package com.shoutit.app.android.view.listeners;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class ListenersPresenter extends BaseProfileListPresenter {

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;

    public ListenersPresenter(@Nonnull ListenersDaos dao,
                              @Nonnull @UiScheduler final Scheduler uiScheduler,
                              @Nonnull ListeningHalfPresenter listeningHalfPresenter,
                              @Nonnull String userName,
                              @Nonnull UserPreferences userPreferences) {
        super(listeningHalfPresenter, uiScheduler, null, userPreferences);

        daoObservable = Observable.just(dao.getDao(userName));

        init();
    }

    @Override
    @Nonnull
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }

}
