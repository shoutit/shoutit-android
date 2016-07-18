package com.shoutit.app.android.view.listenings;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class ListeningsPresenter extends BaseProfileListPresenter {

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;

    public ListeningsPresenter(@UiScheduler final Scheduler uiScheduler,
                               @Nonnull final ListeningsDao listeningsDao,
                               ListeningHalfPresenter listeningHalfPresenter,
                               UserPreferences userPreferences) {
        super(listeningHalfPresenter, uiScheduler, null, userPreferences);

        @SuppressWarnings("ConstantConditions") final String username = userPreferences.getUserOrPage().getUsername();

        daoObservable = Observable.just(
                listeningsDao.getDao(username))
                .compose(ObservableExtensions.behaviorRefCount());

        init();
    }

    @Override
    protected BaseAdapterItem createAdapterItem(BaseProfile profile) {
        return new ListeningsProfileAdapterItem(
                profile, profileSelectedSubject, getListeningHalfPresenter().getListenProfileSubject()
                , actionOnlyForLoggedInUsers, true, false);
    }

    @Nonnull
    public Observable<BaseProfile> getOpenProfileObservable() {
        return profileSelectedSubject;
    }

    @Override
    @Nonnull
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }
}
