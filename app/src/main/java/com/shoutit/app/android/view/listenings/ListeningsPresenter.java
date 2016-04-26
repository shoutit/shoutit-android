package com.shoutit.app.android.view.listenings;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ListeningsPresenter {

    public enum ListeningsType {
        USERS, PAGES, TAGS
    }

    private Observable<List<BaseAdapterItem>> adapterItemsObservable;
    private Observable<Boolean> progressObservable;
    private Observable<Throwable> errorObservable;

    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    @Nonnull
    private final ListeningsDao listeningsDao;
    @Nonnull
    private final ListeningsType listeningsType;

    @Inject
    public ListeningsPresenter(@UiScheduler final Scheduler uiScheduler,
                               @Nonnull final ListeningsDao listeningsDao,
                               @Nonnull ListeningsType listeningsType) {
        this.listeningsDao = listeningsDao;
        this.listeningsType = listeningsType;

        final Observable<ResponseOrError<ListeningResponse>> profilesAndTagsObservable = listeningsDao
                .getDao(listeningsType)
                .getListeningObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ListeningResponse>>behaviorRefCount());

        final Observable<ListeningResponse> successProfilesAndTagsObservable = profilesAndTagsObservable
                .compose(ResponseOrError.<ListeningResponse>onlySuccess());

        adapterItemsObservable = successProfilesAndTagsObservable
                .map(new Func1<ListeningResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ListeningResponse listeningResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        if (listeningResponse.getUsers() != null) {
                            for (User user : listeningResponse.getUsers()) {
                                builder.add(new ProfileAdapterItem(user, openProfileSubject));
                            }
                        }

                        if (listeningResponse.getPages() != null) {
                            for (Page page : listeningResponse.getPages()) {
                                builder.add(new ProfileAdapterItem(page, openProfileSubject));
                            }
                        }

                        if (listeningResponse.getTags() != null) {
                            for (TagDetail tag : listeningResponse.getTags()) {
                                builder.add(new ProfileAdapterItem(tag, openProfileSubject));
                            }
                        }

                        final ImmutableList<BaseAdapterItem> items = builder.build();
                        if (items.size() == 0) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataAdapterItem());
                        } else {
                            return items;
                        }
                    }
                });

        progressObservable = successProfilesAndTagsObservable.map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = profilesAndTagsObservable.compose(ResponseOrError.<ListeningResponse>onlyError());
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return adapterItemsObservable;
    }

    public Observable<String> getProfileToOpenObservable() {
        return openProfileSubject;
    }

    public void refreshData() {
        listeningsDao.getDao(listeningsType)
                .getRefreshSubject().onNext(null);
    }

    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(listeningsDao.getDao(listeningsType)
                .getLoadMoreObserver());
    }
}
