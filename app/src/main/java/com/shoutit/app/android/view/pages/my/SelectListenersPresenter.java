package com.shoutit.app.android.view.pages.my;

import android.support.annotation.NonNull;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Scheduler;

public class SelectListenersPresenter extends BaseProfileListPresenter {

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;

    public SelectListenersPresenter(@Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                    @Nonnull @UiScheduler Scheduler uiScheduler,
                                    @Nullable String placeholderText,
                                    @Nonnull UserPreferences userPreferences,
                                    @Nonnull ListenersDaos listenersDaos) {
        super(listeningHalfPresenter, uiScheduler, placeholderText, userPreferences);

        final String username = userPreferences.getUser().getUsername();

        daoObservable = Observable.just(listenersDaos.getDao(username));

        init();
    }

    @NonNull
    @Override
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }
}
