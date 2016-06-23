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
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.PagesResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.PagesDao;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class MyPagesPresenter {

    @Nonnull
    private final PagesDao pagesDao;
    @Nonnull
    private final Scheduler uiScheduler;

    private final PublishSubject<Page> pageSelctedSubject = PublishSubject.create();
    private final Observable<List<BaseAdapterItem>> pagesObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;

    public MyPagesPresenter(@Nonnull PagesDao pagesDao,
                            @Nonnull @UiScheduler Scheduler uiScheduler,
                            @Nonnull @ForActivity Resources resources) {
        this.pagesDao = pagesDao;
        this.uiScheduler = uiScheduler;

        final Observable<ResponseOrError<PagesResponse>> requestObservable = pagesDao
                .getPagesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.behaviorRefCount());

        pagesObservable = requestObservable
                .compose(ResponseOrError.onlySuccess())
                .map((Func1<PagesResponse, List<BaseAdapterItem>>) pagesResponse -> {
                    final List<Page> pages = pagesResponse.getResults();

                    if (pages.isEmpty()) {
                        return ImmutableList.of(new NoDataTextAdapterItem(resources.getString(R.string.pages_my_empty)));
                    } else {
                        return ImmutableList.copyOf(
                                Lists.transform(pages, new Function<Page, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(Page page) {
                                return new PageAdapterItem(page, pageSelctedSubject);
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

    public Observable<Page> getPageSelctedObservable() {
        return pageSelctedSubject;
    }

    public static class PageAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Page page;
        @Nonnull
        private final Observer<Page> pageSelectedObserver;

        public PageAdapterItem(@Nonnull Page page,
                               @Nonnull Observer<Page> pageSelectedObserver) {
            this.page = page;
            this.pageSelectedObserver = pageSelectedObserver;
        }

        @Nonnull
        public Page getPage() {
            return page;
        }

        public void onPagesSelected() {
            pageSelectedObserver.onNext(page);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof PageAdapterItem &&
                    ((PageAdapterItem) baseAdapterItem).page.getId().equals(page.getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof PageAdapterItem &&
                    ((PageAdapterItem) baseAdapterItem).page.equals(page);
        }
    }
}
