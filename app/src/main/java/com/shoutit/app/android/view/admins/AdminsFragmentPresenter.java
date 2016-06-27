package com.shoutit.app.android.view.admins;

import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;

public class AdminsFragmentPresenter extends BaseProfileListPresenter {

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;

    @Inject
    public AdminsFragmentPresenter(@Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                   @Nonnull ProfilesDao profilesDao,
                                   @Nonnull String placeholderText,
                                   @Nonnull @UiScheduler Scheduler uiScheduler,
                                   @Nonnull UserPreferences userPreferences) {
        super(listeningHalfPresenter, uiScheduler, placeholderText, userPreferences);

        daoObservable = userPreferences.getPageObservable()
                .filter(Functions1.isNotNull())
                .distinctUntilChanged()
                .map(page -> profilesDao.getAdminsDao(page.getUsername()))
                .compose(ObservableExtensions.behaviorRefCount());

        init();
    }

    @NonNull
    @Override
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }
}
