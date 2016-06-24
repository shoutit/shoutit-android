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
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.ProfilesHelper;
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

public class ListeningsPresenter extends ProfilesListPresenter {

    public enum ListeningsType {
        USERS_AND_PAGES, INTERESTS
    }

    private Observable<List<BaseAdapterItem>> adapterItemsObservable;
    private Observable<Boolean> progressObservable;
    private Observable<Throwable> errorObservable;

    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<BaseProfile> profileListenedSubject = PublishSubject.create();
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();
    private final PublishSubject<Object> actionOnlyForLoggedInUser = PublishSubject.create();
    
    @Nonnull
    private final ListeningsDao listeningsDao;
    @Nonnull
    private final ListeningsType listeningsType;
    @Nonnull
    private final ApiService apiService;

    public ListeningsPresenter(@UiScheduler final Scheduler uiScheduler,
                               @Nonnull final ListeningsDao listeningsDao,
                               @Nonnull final ListeningsType listeningsType,
                               @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                               ListeningHalfPresenter listeningHalfPresenter,
                               @Nonnull final ApiService apiService) {
        super(listeningHalfPresenter);
        this.listeningsDao = listeningsDao;
        this.listeningsType = listeningsType;
        this.apiService = apiService;

        final Observable<ResponseOrError<ProfilesListResponse>> profilesAndTagsObservable = listeningsDao
                .getDao(listeningsType)
                .getListeningObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ProfilesListResponse>>behaviorRefCount());

        final Observable<ProfilesListResponse> successProfilesAndTagsObservable = profilesAndTagsObservable
                .compose(ResponseOrError.<ProfilesListResponse>onlySuccess());

        adapterItemsObservable = successProfilesAndTagsObservable
                .map(new Func1<ProfilesListResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ProfilesListResponse listeningResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        for (BaseProfile profile : listeningResponse.getResults()) {
                            builder.add(new ListeningsProfileAdapterItem(
                                    profile, openProfileSubject, profileListenedSubject, listeningsType,
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

        profileListenedSubject
                .withLatestFrom(successProfilesAndTagsObservable, new Func2<BaseProfile, ProfilesListResponse, ProfilesHelper.ProfileToListenWithLastResponse>() {
                    @Override
                    public ProfilesHelper.ProfileToListenWithLastResponse call(BaseProfile profileToListen, ProfilesListResponse listeningResponse) {
                        return new ProfilesHelper.ProfileToListenWithLastResponse(profileToListen, listeningResponse);
                    }
                })
                .switchMap(new Func1<ProfilesHelper.ProfileToListenWithLastResponse, Observable<ResponseOrError<ProfilesListResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ProfilesListResponse>> call(final ProfilesHelper.ProfileToListenWithLastResponse profileToListenWithLastResponse) {

                        final String profileId;
                        if (listeningsType.equals(ListeningsType.INTERESTS)) {
                            profileId = profileToListenWithLastResponse.getProfile().getName();
                        } else {
                            profileId = profileToListenWithLastResponse.getProfile().getUsername();
                        }

                        final boolean isListeningToProfile = profileToListenWithLastResponse.getProfile().isListening();

                        Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                        if (isListeningToProfile) {
                            listenRequestObservable = getUnlistenRequest(profileId)
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
                            listenRequestObservable = getListenRequest(profileId)
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
                                .map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<ProfilesListResponse>>() {
                                    @Override
                                    public ResponseOrError<ProfilesListResponse> call(ResponseOrError<ResponseBody> response) {
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

        errorObservable = profilesAndTagsObservable.compose(ResponseOrError.<ProfilesListResponse>onlyError())
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
    public Observable<ResponseBody> getListenRequest(@Nonnull String userNameOrTagName) {
        if (listeningsType.equals(ListeningsType.INTERESTS)) {
            return apiService.listenTag(userNameOrTagName);
        } else {
            return apiService.listenProfile(userNameOrTagName);
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

    private ProfilesListResponse updateLastResponse(ProfilesHelper.ProfileToListenWithLastResponse profileToListenWithLastResponse) {
        final ProfilesListResponse response = profileToListenWithLastResponse.getResponse();

        final List<BaseProfile> profiles = response.getResults();
        final String profileToUpdateId = getProfileId(profileToListenWithLastResponse.getProfile());

        for (int i = 0; i < profiles.size(); i++) {
            if (getProfileId(profiles.get(i)).equals(profileToUpdateId)) {
                final BaseProfile profileToUpdate = profiles.get(i);
                final BaseProfile updatedProfile = profileToUpdate.getListenedProfile();

                final List<BaseProfile> updatedProfiles = new ArrayList<>(profiles);
                updatedProfiles.set(i, updatedProfile);

                return new ProfilesListResponse(response.getCount(), response.getNext(),
                        response.getPrevious(), updatedProfiles);
            }
        }

        return response;
    }

    public String getProfileId(BaseProfile profile) {
        if (listeningsType.equals(ListeningsType.INTERESTS)) {
            return profile.getName();
        } else {
            return profile.getUsername();
        }
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
