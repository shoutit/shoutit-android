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
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class ListeningHalfPresenter {

    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();
    private final PublishSubject<BaseProfile> listenProfileSubject = PublishSubject.create();

    @Nonnull
    private final ApiService apiService;
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

                    final String profileId = profileToListenWithLastResponse.getProfile().getUsername();
                    final boolean isListeningToProfile = profileToListenWithLastResponse.getProfile().isListening();

                    Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                    if (isListeningToProfile) {
                        listenRequestObservable = apiService.unlistenProfile(profileId)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .doOnNext(responseBody -> unListenSuccess.onNext(profileToListenWithLastResponse.getProfile().getName()))
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                    } else {
                        listenRequestObservable = apiService.listenProfile(profileId)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .doOnNext(responseBody -> listenSuccess.onNext(profileToListenWithLastResponse.getProfile().getName()))
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                    }

                    return listenRequestObservable
                            .map(response -> {
                                if (response.isData()) {
                                    return ResponseOrError.fromData(ProfilesHelper.updateLastResponseWithListenedProfiles(profileToListenWithLastResponse));
                                } else {
                                    errorSubject.onNext(new Throwable());
                                    // On error return current user in order to select/deselect already deselected/selected item
                                    return ResponseOrError.fromData(profileToListenWithLastResponse.getResponse());
                                }
                            });
                });
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

    public PublishSubject<BaseProfile> getListenProfileSubject() {
        return listenProfileSubject;
    }
}
