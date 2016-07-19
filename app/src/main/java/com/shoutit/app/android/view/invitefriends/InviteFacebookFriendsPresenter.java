package com.shoutit.app.android.view.invitefriends;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.InvitationCodeResponse;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class InviteFacebookFriendsPresenter {

    private final PublishSubject<Object> initFBFriendInvite = PublishSubject.create();

    @Nonnull
    private final Observable<String> invitationCodeObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<Throwable> errorObservable;

    @Inject
    public InviteFacebookFriendsPresenter(ApiService apiService,
                                          @UiScheduler Scheduler uiScheduler,
                                          @NetworkScheduler Scheduler networkScheduler) {

        final Observable<ResponseOrError<InvitationCodeResponse>> invitationCodeRequest = initFBFriendInvite
                .switchMap(o -> apiService.getInvitationCode()
                        .subscribeOn(networkScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable())
                        .observeOn(uiScheduler))
                .compose(ObservableExtensions.behaviorRefCount());

        invitationCodeObservable = invitationCodeRequest
                .compose(ResponseOrError.onlySuccess())
                .map(InvitationCodeResponse::getCode);

        progressObservable = Observable.merge(
                initFBFriendInvite.map(Functions1.returnTrue()),
                invitationCodeRequest.map(Functions1.returnFalse())
        );

        errorObservable = invitationCodeRequest
                .compose(ResponseOrError.onlyError());

    }

    @Nonnull
    public Observable<String> getInvitationCodeObservable() {
        return invitationCodeObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public void initFbFriendInvite() {
        initFBFriendInvite.onNext(null);
    }

    public Observer<Object> getInitFBFriendInviteObserver() {
        return initFBFriendInvite;
    }
}
