package com.shoutit.app.android.view.listeners;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.adapteritems.BaseProfileAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ListenersPresenter extends ProfilesListPresenter {

    @Nonnull
    private final ListenersDaos dao;
    @Nonnull
    private final String userName;
    @Nonnull
    private Observable<List<BaseAdapterItem>> adapterItemsObservable;
    @Nonnull
    private Observable<Boolean> progressObservable;
    @Nonnull
    private Observable<Throwable> errorObservable;

    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    private final PublishSubject<Object> actionOnlyForLoggedInUser = PublishSubject.create();

    public ListenersPresenter(@Nonnull ListenersDaos dao,
                              @Nonnull @UiScheduler final Scheduler uiScheduler,
                              @Nonnull ListeningHalfPresenter listeningHalfPresenter,
                              @Nonnull String userName) {
        super(listeningHalfPresenter);
        this.dao = dao;
        this.userName = userName;

        final Observable<ResponseOrError<ProfilesListResponse>> listenersRequest = dao.getDao(userName)
                .getLstenersObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ProfilesListResponse>>behaviorRefCount());

        final Observable<ProfilesListResponse> successListeners = listenersRequest
                .compose(ResponseOrError.<ProfilesListResponse>onlySuccess());

        adapterItemsObservable = successListeners
                .map(new Func1<ProfilesListResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ProfilesListResponse listeningResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        for (BaseProfile profile : listeningResponse.getResults()) {
                            builder.add(new ListenersAdapterItem(
                                    profile, openProfileSubject, listeningHalfPresenter.getListenProfileSubject(),
                                    actionOnlyForLoggedInUser, true, false));
                        }

                        final ImmutableList<BaseAdapterItem> items = builder.build();
                        if (items.size() == 0) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataAdapterItem());
                        } else {
                            return items;
                        }
                    }
                });

        listeningHalfPresenter
                .listeningObservable(successListeners)
                .subscribe(dao.getDao(userName).getUpdateResponseLocally());

        progressObservable = successListeners.map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = listenersRequest.compose(ResponseOrError.<ProfilesListResponse>onlyError())
                .mergeWith(listeningHalfPresenter.getErrorSubject());
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
    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return adapterItemsObservable;
    }

    @Nonnull
    public Observable<String> getProfileToOpenObservable() {
        return openProfileSubject;
    }

    public void refreshData() {
        dao.getDao(userName)
                .getRefreshSubject()
                .onNext(null);
    }

    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(dao.getDao(userName)
                .getLoadMoreObserver());
    }

    public static class ListenersAdapterItem extends BaseProfileAdapterItem {

        @Nonnull
        private final BaseProfile profile;
        @Nonnull
        private final Observer<String> openProfileObserver;
        @Nonnull
        private final Observer<BaseProfile> profileListenedObserver;


        public ListenersAdapterItem(@Nonnull BaseProfile profile,
                                    @Nonnull Observer<String> openProfileObserver,
                                    @Nonnull Observer<BaseProfile> profileListenedObserver,
                                    @Nonnull Observer<Object> actionOnlyForLoggedInUsers,
                                    boolean isNormalUser,
                                    boolean isProfileMine) {
            super(profile, profileListenedObserver, actionOnlyForLoggedInUsers, isNormalUser, isProfileMine);
            this.profile = profile;
            this.openProfileObserver = openProfileObserver;
            this.profileListenedObserver = profileListenedObserver;
        }

        @Override
        public void openProfile() {
            openProfileObserver.onNext(profile.getUsername());
        }

        public void onProfileListened() {
            profileListenedObserver.onNext(profile);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ListenersAdapterItem &&
                    profile.getUsername().equals(((ListenersAdapterItem) item).getProfile().getUsername());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ListenersAdapterItem &&
                    profile.equals(((ListenersAdapterItem) item).getProfile());
        }
    }
}
