package com.shoutit.app.android.view.pages.publics;

import android.content.res.Resources;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.PagesResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.PublicPagesDaos;
import com.shoutit.app.android.view.pages.PagesPresenter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;

public class PublicPagesPresenter extends PagesPresenter {

    private final Observable<Object> loadMoreObservable;
    private final Observable<ResponseOrError<PagesResponse>> requestObservable;

    @Inject
    public PublicPagesPresenter(@Nonnull PublicPagesDaos pagesDaos,
                                @Nonnull @UiScheduler Scheduler uiScheduler,
                                @Nonnull @ForActivity Resources resources,
                                @Nonnull UserPreferences userPreferences) {
        super(uiScheduler, resources);

        final Observable<PublicPagesDaos.PublicPagesDao> daoObservable = userPreferences
                .getUserObservable()
                .filter(Functions1.isNotNull())
                .map(User::getLocation)
                .filter(Functions1.isNotNull())
                .map(UserLocation::getCountry)
                .map(pagesDaos::getDao)
                .compose(ObservableExtensions.behaviorRefCount());

        requestObservable = daoObservable
                .flatMap(PublicPagesDaos.PublicPagesDao::getPagesObservable);

        loadMoreObservable = loadMoreSubject
                .withLatestFrom(daoObservable, (ignore, dao) -> {
                    dao.getLoadMoreSubject().onNext(null);
                    return null;
                });

        init();
    }

    @Override
    public Observable<ResponseOrError<PagesResponse>> getRequestObservable() {
        return requestObservable;
    }

    public Observable<Object> getLoadMoreObservable() {
        return loadMoreObservable;
    }
}
