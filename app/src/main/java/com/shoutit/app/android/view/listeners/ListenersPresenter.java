package com.shoutit.app.android.view.listeners;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.adapteritems.BaseProfileAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ListenersResponse;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class ListenersPresenter implements ProfilesListPresenter {

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
    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<BaseProfile> profileListenedSubject = PublishSubject.create();
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();

    public ListenersPresenter(@Nonnull ListenersDaos dao,
                              @Nonnull final ApiService apiService,
                              @Nonnull @UiScheduler final Scheduler uiScheduler,
                              @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                              @Nonnull String userName) {
        this.dao = dao;
        this.userName = userName;

        final Observable<ResponseOrError<ListenersResponse>> listenersRequest = dao.getDao(userName)
                .getLstenersObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ListenersResponse>>behaviorRefCount());

        final Observable<ListenersResponse> successListeners = listenersRequest
                .compose(ResponseOrError.<ListenersResponse>onlySuccess());

        adapterItemsObservable = successListeners
                .map(new Func1<ListenersResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ListenersResponse listeningResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        for (BaseProfile profile : listeningResponse.getProfiles()) {
                            builder.add(new ListenersAdapterItem(
                                    profile, openProfileSubject, profileListenedSubject));
                        }

                        final ImmutableList<BaseAdapterItem> items = builder.build();
                        if (items.size() == 0) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataAdapterItem());
                        } else {
                            return items;
                        }
                    }
                });

        profileListenedSubject
                .withLatestFrom(successListeners, new Func2<BaseProfile, ListenersResponse, ProfileToListenWithLastResponse>() {
                    @Override
                    public ProfileToListenWithLastResponse call(BaseProfile profileToListen, ListenersResponse listeningResponse) {
                        return new ProfileToListenWithLastResponse(profileToListen, listeningResponse);
                    }
                })
                .switchMap(new Func1<ProfileToListenWithLastResponse, Observable<ResponseOrError<ListenersResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ListenersResponse>> call(final ProfileToListenWithLastResponse profileToListenWithLastResponse) {

                        final String profileId = profileToListenWithLastResponse.getProfile().getUsername();
                        final boolean isListeningToProfile = profileToListenWithLastResponse.getProfile().isListening();

                        Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                        if (isListeningToProfile) {
                            listenRequestObservable = apiService.unlistenProfile(profileId)
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .doOnNext(new Action1<ResponseBody>() {
                                        @Override
                                        public void call(ResponseBody responseBody) {
                                            unListenSuccess.onNext(profileToListenWithLastResponse.getProfile().getName());
                                        }
                                    })
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        } else {
                            listenRequestObservable = apiService.listenProfile(profileId)
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .doOnNext(new Action1<ResponseBody>() {
                                        @Override
                                        public void call(ResponseBody responseBody) {
                                            listenSuccess.onNext(profileToListenWithLastResponse.getProfile().getName());
                                        }
                                    })
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        }

                        return listenRequestObservable
                                .map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<ListenersResponse>>() {
                                    @Override
                                    public ResponseOrError<ListenersResponse> call(ResponseOrError<ResponseBody> response) {
                                        if (response.isData()) {
                                            return ResponseOrError.fromData(updateLastResponse(profileToListenWithLastResponse));
                                        } else {
                                            errorSubject.onNext(new Throwable());
                                            // On error return current user in order to select/deselect already deselected/selected item
                                            return ResponseOrError.fromData(profileToListenWithLastResponse.getResponse());
                                        }
                                    }
                                });
                    }
                })
                .subscribe(dao.getDao(userName).getUpdateResponseLocally());

        progressObservable = successListeners.map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = listenersRequest.compose(ResponseOrError.<ListenersResponse>onlyError())
                .mergeWith(errorSubject);
    }

    private ListenersResponse updateLastResponse(ProfileToListenWithLastResponse profileToListenWithLastResponse) {
        final ListenersResponse response = profileToListenWithLastResponse.getResponse();

        final List<BaseProfile> profiles = response.getProfiles();
        final String profileToUpdateId = profileToListenWithLastResponse.getProfile().getUsername();

        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getUsername().equals(profileToUpdateId)) {
                final BaseProfile profileToUpdate = profiles.get(i);
                final BaseProfile updatedProfile = profileToUpdate.getListenedProfile();

                final List<BaseProfile> updatedProfiles = new ArrayList<>(profiles);
                updatedProfiles.set(i, updatedProfile);

                return new ListenersResponse(response.getCount(), response.getNext(),
                        response.getPrevious(), updatedProfiles);
            }
        }

        return response;
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
    public Observable<String> getListenSuccessObservable() {
        return listenSuccess;
    }

    @Nonnull
    public Observable<String> getUnListenSuccessObservable() {
        return unListenSuccess;
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
                                    @Nonnull Observer<BaseProfile> profileListenedObserver) {
            super(profile, profileListenedObserver);
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

    public static class ProfileToListenWithLastResponse {

        @Nonnull
        private final BaseProfile profile;
        @Nonnull
        private final ListenersResponse response;

        public ProfileToListenWithLastResponse(@Nonnull BaseProfile profile,
                                               @Nonnull ListenersResponse response) {
            this.profile = profile;
            this.response = response;
        }

        @Nonnull
        public BaseProfile getProfile() {
            return profile;
        }

        @Nonnull
        public ListenersResponse getResponse() {
            return response;
        }
    }
}
