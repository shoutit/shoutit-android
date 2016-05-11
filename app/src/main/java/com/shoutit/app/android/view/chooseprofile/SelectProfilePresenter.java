package com.shoutit.app.android.view.chooseprofile;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ListenersResponse;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.view.listenings.ListeningsPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class SelectProfilePresenter {

    @Nonnull
    private final Observable<List<BaseAdapterItem>> listeningsAdapterItems;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> listenersAdapterItems;
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;

    private PublishSubject<String> profileSelectedSubject = PublishSubject.create();
    private PublishSubject<Object> loadMoreListenings = PublishSubject.create();
    private PublishSubject<Object> loadMoreListeners = PublishSubject.create();

    @Inject
    public SelectProfilePresenter(@Nonnull ListeningsDao listeningsDao,
                                  @Nonnull ListenersDaos listenersDao,
                                  @UiScheduler Scheduler uiScheduler) {

        final Observable<ResponseOrError<ListeningResponse>> listeningsObservable = listeningsDao
                .getDao(ListeningsPresenter.ListeningsType.USERS_AND_PAGES)
                .getListeningObservable()
                .subscribeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ListeningResponse>>behaviorRefCount());

        final Observable<ResponseOrError<ListenersResponse>> listenersObservable = listenersDao
                .getDao(User.ME)
                .getLstenersObservable()
                .subscribeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ListenersResponse>>behaviorRefCount());

        listeningsAdapterItems = listeningsObservable
                .compose(ResponseOrError.<ListeningResponse>onlySuccess())
                .map(new Func1<ListeningResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ListeningResponse listeningResponse) {
                        return itemsToAdapterItem(listeningResponse.getProfiles());
                    }
                });

        listenersAdapterItems = listenersObservable
                .compose(ResponseOrError.<ListenersResponse>onlySuccess())
                .map(new Func1<ListenersResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ListenersResponse listenersResponse) {
                        return itemsToAdapterItem(listenersResponse.getProfiles());
                    }
                });

        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(listenersObservable),
                        ResponseOrError.transform(listeningsObservable)
                )
        ).filter(Functions1.isNotNull());

        progressObservable = Observable.merge(listenersObservable, listeningsObservable)
                .map(Functions1.returnFalse())
                .startWith(true);

    }

    public Observer<Object> getLoadMoreListeners() {
        return loadMoreListeners;
    }

    public Observer<Object> getLoadMoreListenings() {
        return loadMoreListenings;
    }

    private List<BaseAdapterItem> itemsToAdapterItem(@Nonnull List<BaseProfile> items) {
        if (items.isEmpty()) {
            return ImmutableList.<BaseAdapterItem>of(new NoDataAdapterItem());
        } else {
            return Lists.transform(items, new Function<BaseProfile, BaseAdapterItem>() {
                @Override
                public SelectProfileAdpaterItem apply(BaseProfile input) {
                    return new SelectProfileAdpaterItem(profileSelectedSubject, input);
                }
            });
        }
    }
}
