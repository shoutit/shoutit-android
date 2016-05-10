package com.shoutit.app.android.view.listenings;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ListeningsPresenter {

    public enum ListeningsType {
        USERS_AND_PAGES, INTERESTS
    }

    private Observable<List<BaseAdapterItem>> adapterItemsObservable;
    private Observable<Boolean> progressObservable;
    private Observable<Throwable> errorObservable;

    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<ProfileToListenWithLastResponse> profileListenedSubject = PublishSubject.create();
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();
    
    @Nonnull
    private final ListeningsDao listeningsDao;
    @Nonnull
    private final ListeningsType listeningsType;
    @Nonnull
    private final ApiService apiService;

    public ListeningsPresenter(@UiScheduler final Scheduler uiScheduler,
                               @Nonnull final ListeningsDao listeningsDao,
                               @Nonnull ListeningsType listeningsType,
                               @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                               @Nonnull final ApiService apiService) {
        this.listeningsDao = listeningsDao;
        this.listeningsType = listeningsType;
        this.apiService = apiService;

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

                        for (BaseProfile profile : listeningResponse.getProfiles()) {
                            builder.add(new ProfileAdapterItem(profile, openProfileSubject, profileListenedSubject, listeningResponse));
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
                .switchMap(new Func1<ProfileToListenWithLastResponse, Observable<ResponseOrError<ListeningResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ListeningResponse>> call(final ProfileToListenWithLastResponse profileToListenWithLastResponse) {
                        final String userName = profileToListenWithLastResponse.getProfile().getUsername();
                        final boolean isListeningToProfile = profileToListenWithLastResponse.getProfile().isListening();

                        Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                        if (isListeningToProfile) {
                            listenRequestObservable = getUnlistenRequest(userName)
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
                            listenRequestObservable = getListenRequest(userName)
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
                                .map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<ListeningResponse>>() {
                                    @Override
                                    public ResponseOrError<ListeningResponse> call(ResponseOrError<ResponseBody> response) {
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
                .subscribe(listeningsDao.getDao(listeningsType).updatedResponseLocallyObserver());

        progressObservable = successProfilesAndTagsObservable.map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = profilesAndTagsObservable.compose(ResponseOrError.<ListeningResponse>onlyError())
                .mergeWith(errorSubject);
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
    public Observable<ResponseBody> getListenRequest(@Nonnull String userOrTagName) {
        if (listeningsType.equals(ListeningsType.INTERESTS)) {
            return apiService.listenTag(userOrTagName);
        } else {
            return apiService.listenProfile(userOrTagName);
        }
    }

    @Nonnull
    public Observable<ResponseBody> getUnlistenRequest(@Nonnull String userOrTagName) {
        if (listeningsType.equals(ListeningsType.INTERESTS)) {
            return apiService.unlistenTag(userOrTagName);
        } else {
            return apiService.unlistenProfile(userOrTagName);
        }
    }

    private ListeningResponse updateLastResponse(ProfileToListenWithLastResponse profileToListenWithLastResponse) {
        final ListeningResponse response = profileToListenWithLastResponse.getResponse();

        final List<BaseProfile> profiles = response.getProfiles();
        final String profileToUpdateUserName = profileToListenWithLastResponse.getProfile().getUsername();

        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getUsername().equals(profileToUpdateUserName)) {
                final BaseProfile profileToUpdate = profiles.get(i);
                final BaseProfile updatedProfile = profileToUpdate.getListenedProfile();

                final List<BaseProfile> updatedProfiles = new ArrayList<>(profiles);
                updatedProfiles.set(i, updatedProfile);

                return new ListeningResponse(response.getCount(), response.getNext(),
                        response.getPrevious(), updatedProfiles);
            }
        }

        return response;
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

    public static class ProfileToListenWithLastResponse {

        @Nonnull
        private final BaseProfile profile;
        @Nonnull
        private final ListeningResponse response;

        public ProfileToListenWithLastResponse(@Nonnull BaseProfile profile,
                                               @Nonnull ListeningResponse response) {
            this.profile = profile;
            this.response = response;
        }

        @Nonnull
        public BaseProfile getProfile() {
            return profile;
        }

        @Nonnull
        public ListeningResponse getResponse() {
            return response;
        }
    }
}
