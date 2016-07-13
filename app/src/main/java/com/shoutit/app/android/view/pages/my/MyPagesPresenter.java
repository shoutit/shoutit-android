package com.shoutit.app.android.view.pages.my;

import android.content.res.Resources;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.PagesResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.PagesDao;
import com.shoutit.app.android.view.pages.PageAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class MyPagesPresenter {

    private final Observable<Object> loadMoreObservable;
    @Nonnull
    private final PagesDao pagesDao;
    private Observable<List<BaseAdapterItem>> pagesObservable;
    private Observable<Boolean> progressObservable;
    private Observable<Throwable> errorObservable;

    private final PublishSubject<Page> pageSelectedSubject = PublishSubject.create();
    protected final PublishSubject<Object> loadMoreSubject = PublishSubject.create();

    @Inject
    public MyPagesPresenter(@Nonnull PagesDao pagesDao,
                            @Nonnull @UiScheduler Scheduler uiScheduler,
                            @Nonnull @ForActivity Resources resources) {
        this.pagesDao = pagesDao;

        final Observable<ResponseOrError<PagesResponse>> requestObservable =
                pagesDao.getPagesObservable()
                        .observeOn(uiScheduler)
                        .compose(ObservableExtensions.behaviorRefCount());

        pagesObservable = requestObservable
                .compose(ResponseOrError.onlySuccess())
                .map((Func1<PagesResponse, List<BaseAdapterItem>>) pagesResponse -> {
                    final List<Page> pages = pagesResponse.getResults();

                    if (pages.isEmpty()) {
                        return ImmutableList.of(new NoDataTextAdapterItem(resources.getString(R.string.pages_empty)));
                    } else {
                        return ImmutableList.copyOf(
                                Lists.transform(pages, new Function<Page, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public BaseAdapterItem apply(Page page) {
                                        return new PageAdapterItem(page, pageSelectedSubject);
                                    }
                                }));
                    }
                });

        errorObservable = requestObservable
                .compose(ResponseOrError.onlyError());

        progressObservable = requestObservable.map(Functions1.returnFalse())
                .startWith(true);

        loadMoreObservable = loadMoreSubject
                .flatMap(o -> {
                    pagesDao.getLoadMoreSubject().onNext(null);
                    return null;
                });
    }

    public Observable<Object> getLoadMoreObservable() {
        return loadMoreObservable;
    }

    public Observable<List<BaseAdapterItem>> getPagesObservable() {
        return pagesObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<Page> getPageSelectedObservable() {
        return pageSelectedSubject;
    }

    public Observer<Object> getLoadMoreObserver() {
        return loadMoreSubject;
    }

    public void refreshData() {
        pagesDao.getRefreshSubject().onNext(null);
    }
}
