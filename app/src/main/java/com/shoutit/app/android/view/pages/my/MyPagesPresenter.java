package com.shoutit.app.android.view.pages.my;

import android.content.res.Resources;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.model.PagesResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.PagesDao;
import com.shoutit.app.android.view.pages.PagesPresenter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;

public class MyPagesPresenter extends PagesPresenter {

    @Nonnull
    private final PagesDao pagesDao;
    private final Observable<Object> loadMoreObservable;

    @Inject
    public MyPagesPresenter(@Nonnull PagesDao pagesDao,
                            @Nonnull @UiScheduler Scheduler uiScheduler,
                            @Nonnull @ForActivity Resources resources) {
        super(uiScheduler, resources);
        this.pagesDao = pagesDao;

        loadMoreObservable = loadMoreSubject
                .flatMap(o -> {
                    pagesDao.getLoadMoreSubject().onNext(null);
                    return null;
                });

        init();
    }

    @Override
    public Observable<ResponseOrError<PagesResponse>> getRequestObservable() {
        return pagesDao.getPagesObservable();
    }

    public Observable<Object> getLoadMoreObservable() {
        return loadMoreObservable;
    }
}
