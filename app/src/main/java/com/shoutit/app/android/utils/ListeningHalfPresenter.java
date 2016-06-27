package com.shoutit.app.android.utils;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class ListeningHalfPresenter {

    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();
    private final PublishSubject<BaseProfile> listenProfileSubject = PublishSubject.create();

    @Nonnull
    protected final ApiService apiService;
    private final Scheduler networkScheduler;
    private final Scheduler uiScheduler;

    @Inject
    public ListeningHalfPresenter(@Nonnull ApiService apiService,
                                  @NetworkScheduler Scheduler networkScheduler,
                                  @UiScheduler Scheduler uiScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;
        this.uiScheduler = uiScheduler;
    }

    @Nonnull
    public Observable<ResponseOrError<ProfilesListResponse>> listeningObservable(Observable<ProfilesListResponse> successRequest) {
        return listenProfileSubject
                .withLatestFrom(successRequest, ProfilesHelper.ProfileToListenWithLastResponse::new)
                .switchMap(profileToListenWithLastResponse -> {

                    final boolean isListeningToProfile = profileToListenWithLastResponse.getProfile().isListening();

                    Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                    if (isListeningToProfile) {
                        listenRequestObservable = getUnlistenRequest(profileToListenWithLastResponse.getProfile())
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .doOnNext(responseBody -> unListenSuccess.onNext(profileToListenWithLastResponse.getProfile().getName()))
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                    } else {
                        listenRequestObservable = getListenRequest(profileToListenWithLastResponse.getProfile())
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .doOnNext(responseBody -> listenSuccess.onNext(profileToListenWithLastResponse.getProfile().getName()))
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                    }

                    return listenRequestObservable
                            .map(response -> {
                                if (response.isData()) {
                                    return ResponseOrError.fromData(updateResponseWithListenedProfiles(profileToListenWithLastResponse));
                                } else {
                                    errorSubject.onNext(new Throwable());
                                    // On error return current user in order to select/deselect already deselected/selected item
                                    return ResponseOrError.fromData(profileToListenWithLastResponse.getResponse());
                                }
                            });
                })
                .observeOn(uiScheduler);
    }

    protected ProfilesListResponse updateResponseWithListenedProfiles(@Nonnull ProfilesHelper.ProfileToListenWithLastResponse profileToListenWithLastResponse) {
        return ProfilesHelper.updateLastResponseWithListenedProfiles(profileToListenWithLastResponse);
    }

    @Nonnull
    public PublishSubject<Throwable> getErrorSubject() {
        return errorSubject;
    }

    @Nonnull
    public PublishSubject<String> getListenSuccess() {
        return listenSuccess;
    }

    @Nonnull
    public PublishSubject<String> getUnListenSuccess() {
        return unListenSuccess;
    }

    @Nonnull
    public PublishSubject<BaseProfile> getListenProfileSubject() {
        return listenProfileSubject;
    }

    @Nonnull
    public Observable<ResponseBody> getListenRequest(@Nonnull BaseProfile baseProfile) {
        return apiService.listenProfile(baseProfile.getUsername());
    }

    @Nonnull
    public Observable<ResponseBody> getUnlistenRequest(@Nonnull BaseProfile baseProfile) {
        return apiService.unlistenProfile(baseProfile.getUsername());
    }
}
