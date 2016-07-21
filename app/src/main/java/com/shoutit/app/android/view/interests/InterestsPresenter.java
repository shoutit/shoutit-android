package com.shoutit.app.android.view.interests;

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
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ListenResponse;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.TagsListResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.TagListDaos;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.listenings.ListenTagsHalfPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;


public class InterestsPresenter {

    @Nonnull
    private final ListenTagsHalfPresenter listeningHalfPresenter;
    @Nonnull
    private final Resources resources;

    private Observable<TagListDaos.TagListDao> daoObservable;
    private Observable<Object> refreshDataObservable;
    private Observable<Object> listeningObservable;
    private Observable<Throwable> errorObservable;
    private Observable<Boolean> progressObservable;
    private Observable<Object> loadMoreObservable;

    @Nonnull
    private final PublishSubject<TagDetail> profileSelectedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> refreshDataSubject = PublishSubject.create();
    @Nonnull
    private Observable<TagsListResponse> successRequestObservable;

    @Inject
    public InterestsPresenter(@Nonnull ListenTagsHalfPresenter listeningHalfPresenter,
                              @Nonnull @ForActivity Resources resources,
                              @Nonnull @UiScheduler Scheduler uiScheduler,
                              @Nonnull TagListDaos tagListDaos) {
        this.listeningHalfPresenter = listeningHalfPresenter;
        this.resources = resources;

        daoObservable = Observable.just(tagListDaos.getDao(BaseProfile.ME))
                .compose(ObservableExtensions.behaviorRefCount());

        final Observable<ResponseOrError<TagsListResponse>> requestObservable =
                daoObservable.flatMap(dao -> dao.getTagsObservable()
                        .observeOn(uiScheduler))
                        .compose(ObservableExtensions.behaviorRefCount());

        successRequestObservable = requestObservable
                .compose(ResponseOrError.onlySuccess());

        listeningObservable = listeningHalfPresenter
                .listeningObservable(successRequestObservable)
                .switchMap(updatedProfile -> daoObservable
                        .map((Func1<TagListDaos.TagListDao, TagsListResponse>) dao -> {
                            dao.getUpdatedTagLocallySubject().onNext(updatedProfile);
                            return null;
                        }));

        errorObservable = requestObservable
                .compose(ResponseOrError.onlyError())
                .mergeWith(listeningHalfPresenter.getErrorSubject());

        progressObservable = requestObservable.map(Functions1.returnFalse())
                .startWith(true);

        loadMoreObservable = loadMoreSubject
                .withLatestFrom(daoObservable, (ignore, dao) -> {
                    dao.getLoadMoreSubject().onNext(null);
                    return null;
                });

        refreshDataObservable = refreshDataSubject
                .withLatestFrom(daoObservable, (o, dao) -> {
                    dao.getRefreshSubject().onNext(null);
                    return null;
                });
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return successRequestObservable
                .map((Func1<TagsListResponse, List<BaseAdapterItem>>) pagesResponse -> {
                    final List<TagDetail> pages = pagesResponse.getResults();

                    if (pages.isEmpty()) {
                        return ImmutableList.of(new NoDataTextAdapterItem(resources.getString(R.string.listenings_empty)));
                    } else {
                        return ImmutableList.copyOf(
                                Lists.transform(pages, new Function<TagDetail, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public BaseAdapterItem apply(TagDetail profile) {
                                        return new TagAdapterItem(profile, profileSelectedSubject,
                                                listeningHalfPresenter.getListenTagSubject());
                                    }
                                }));
                    }
                });
    }

    @Nonnull
    public ListenTagsHalfPresenter getListeningHalfPresenter() {
        return listeningHalfPresenter;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    public Observable<TagDetail> getProfileSelectedObservable() {
        return profileSelectedSubject;
    }

    public void refreshData() {
        refreshDataSubject.onNext(null);
    }

    @Nonnull
    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(loadMoreSubject);
    }

    @Nonnull
    public Observable<ListenResponse> getListenSuccessObservable() {
        return listeningHalfPresenter.getListenSuccess();
    }

    @Nonnull
    public Observable<ListenResponse> getUnListenSuccessObservable() {
        return listeningHalfPresenter.getUnListenSuccess();
    }

    @Nonnull
    public Observable<Object> getLoadMoreObservable() {
        return loadMoreObservable;
    }

    @Nonnull
    public Observable<Object> getRefreshDataObservable() {
        return refreshDataObservable;
    }

    @Nonnull
    public Observable<Object> getListeningObservable() {
        return listeningObservable;
    }

    public static class TagAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final TagDetail tag;
        @Nonnull
        private final Observer<TagDetail> profileSelectedObserver;
        @Nonnull
        private final Observer<TagDetail> profileListenedObserver;


        public TagAdapterItem(@Nonnull TagDetail tag,
                              @Nonnull Observer<TagDetail> profileSelectedObserver,
                              @Nonnull Observer<TagDetail> profileListenedObserver) {
            this.tag = tag;
            this.profileSelectedObserver = profileSelectedObserver;
            this.profileListenedObserver = profileListenedObserver;
        }

        @Nonnull
        public TagDetail getTag() {
            return tag;
        }

        public void openTagProfile() {
            profileSelectedObserver.onNext(tag);
        }

        public void onTagListened() {
            profileListenedObserver.onNext(tag);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof TagAdapterItem &&
                    tag.getSlug().equals(((TagAdapterItem) item).tag.getSlug());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof TagAdapterItem &&
                    tag.equals(((TagAdapterItem) item).tag);
        }
    }
}





