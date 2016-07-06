package com.shoutit.app.android.view.chooseprofile;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.model.ListeningsPointer;
import com.shoutit.app.android.view.listenings.ListeningsPresenter;

import java.util.List;

import javax.annotation.Nonnull;

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

    private PublishSubject<BaseProfile> profileSelectedSubject = PublishSubject.create();
    private PublishSubject<Object> loadMoreListenings = PublishSubject.create();
    private PublishSubject<Object> loadMoreListeners = PublishSubject.create();

    public SelectProfilePresenter(@Nonnull ListeningsDao listeningsDao,
                                  @Nonnull ListenersDaos listenersDao,
                                  @UiScheduler Scheduler uiScheduler,
                                  @Nonnull UserPreferences userPreferences) {

        final String userName = userPreferences.getUser().getUsername();

        final Observable<ResponseOrError<ProfilesListResponse>> listeningsObservable = listeningsDao
                .getDao(new ListeningsPointer(ListeningsPresenter.ListeningsType.USERS_AND_PAGES, userName))
                .getProfilesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ProfilesListResponse>>behaviorRefCount());

        final Observable<ResponseOrError<ProfilesListResponse>> listenersObservable = listenersDao
                .getDao(userName)
                .getProfilesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ProfilesListResponse>>behaviorRefCount());

        listeningsAdapterItems = listeningsObservable
                .compose(ResponseOrError.<ProfilesListResponse>onlySuccess())
                .map(new Func1<ProfilesListResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ProfilesListResponse listeningResponse) {
                        return itemsToAdapterItem(listeningResponse.getResults());
                    }
                });

        listenersAdapterItems = listenersObservable
                .compose(ResponseOrError.<ProfilesListResponse>onlySuccess())
                .map(new Func1<ProfilesListResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ProfilesListResponse listenersResponse) {
                        return itemsToAdapterItem(listenersResponse.getResults());
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

    public Observer<Object> getLoadMoreListenersObserver() {
        return loadMoreListeners;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getListeningsAdapterItems() {
        return listeningsAdapterItems;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getListenersAdapterItems() {
        return listenersAdapterItems;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<BaseProfile> getProfileSelectedObservable() {
        return profileSelectedSubject;
    }

    public Observer<Object> getLoadMoreListeningsObserver() {
        return loadMoreListenings;
    }

    private List<BaseAdapterItem> itemsToAdapterItem(@Nonnull List<BaseProfile> items) {
        if (items.isEmpty()) {
            return ImmutableList.<BaseAdapterItem>of(new NoDataAdapterItem());
        } else {
            return Lists.transform(items, new Function<BaseProfile, BaseAdapterItem>() {
                @Override
                public SelectProfileAdapterItem apply(BaseProfile input) {
                    return new SelectProfileAdapterItem(profileSelectedSubject, input);
                }
            });
        }
    }
}
