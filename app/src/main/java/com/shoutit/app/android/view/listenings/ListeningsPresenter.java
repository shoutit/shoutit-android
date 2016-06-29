package com.shoutit.app.android.view.listenings;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.model.ListeningsPointer;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class ListeningsPresenter extends BaseProfileListPresenter {

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;
    @Nonnull
    private final ListeningsType listeningsType;

    public enum ListeningsType {
        USERS_AND_PAGES, INTERESTS
    }

    public ListeningsPresenter(@UiScheduler final Scheduler uiScheduler,
                               @Nonnull final ListeningsDao listeningsDao,
                               @Nonnull final ListeningsType listeningsType,
                               ListenUserOrPageHalfPresenter listeningHalfPresenter,
                               UserPreferences userPreferences) {
        super(listeningHalfPresenter, uiScheduler, null, userPreferences);
        this.listeningsType = listeningsType;

        @SuppressWarnings("ConstantConditions") final String username = userPreferences.getPageOrUser().getUsername();

        daoObservable = Observable.just(
                listeningsDao.getDao(new ListeningsPointer(listeningsType, username)))
                .compose(ObservableExtensions.behaviorRefCount());

        init();
    }

    @Override
    protected BaseAdapterItem createAdapterItem(BaseProfile profile) {
        return new ListeningsProfileAdapterItem(
                profile, profileSelectedSubject, getListeningHalfPresenter().getListenProfileSubject(),
                listeningsType, actionOnlyForLoggedInUsers, true, false);
    }

    @Nonnull
    public Observable<String> getOpenProfileObservable() {
        if (listeningsType.equals(ListeningsPresenter.ListeningsType.INTERESTS)) {
            return profileSelectedSubject.map(BaseProfile::getName);
        } else {
            return profileSelectedSubject.map(BaseProfile::getUsername);
        }
    }

    @Override
    @Nonnull
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }
}
