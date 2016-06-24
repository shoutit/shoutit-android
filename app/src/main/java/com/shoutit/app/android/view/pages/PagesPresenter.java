package com.shoutit.app.android.view.pages;

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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;


public abstract class PagesPresenter {

    private final PublishSubject<Page> pageSelectedSubject = PublishSubject.create();
    protected final PublishSubject<Object> loadMoreSubject = PublishSubject.create();

    private Observable<List<BaseAdapterItem>> pagesObservable;
    private Observable<Boolean> progressObservable;
    private Observable<Throwable> errorObservable;

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final Resources resources;

    public PagesPresenter(@Nonnull @UiScheduler Scheduler uiScheduler,
                          @Nonnull @ForActivity Resources resources) {
        this.uiScheduler = uiScheduler;
        this.resources = resources;
    }

    protected void init() {
        final Observable<ResponseOrError<PagesResponse>> requestObservable =
                getRequestObservable()
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

    public abstract Observable<ResponseOrError<PagesResponse>> getRequestObservable();

}

