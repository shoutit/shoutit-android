package com.shoutit.app.android.view.listenings;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class ListeningsPresenter extends BaseProfileListPresenter {

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;

    public enum ListeningsType {
        USERS_AND_PAGES, INTERESTS
    }

    public ListeningsPresenter(@UiScheduler final Scheduler uiScheduler,
                               @Nonnull final ListeningsDao listeningsDao,
                               @Nonnull final ListeningsType listeningsType,
                               ListenUserOrPageHalfPresenter listeningHalfPresenter,
                               UserPreferences userPreferences) {
        super(listeningHalfPresenter, uiScheduler, null, userPreferences);

        daoObservable = Observable.just(
                listeningsDao.getDao(listeningsType))
                .compose(ObservableExtensions.behaviorRefCount());

        init();
    }

    @Override
    @Nonnull
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }
}
