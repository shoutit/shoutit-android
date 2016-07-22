package com.shoutit.app.android.view.profile.user.userprofile;

import android.content.Context;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ListenResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.profile.ChatInfo;
import com.shoutit.app.android.view.profile.user.ProfileAdapterItems;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class UserProfileHalfPresenter {

    @Nonnull
    private final PublishSubject<Object> moreMenuOptionClickedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> actionOnlyForLoggedInUserSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<ChatInfo> onChatIconClickedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<User> onListenActionClickedSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<ListenResponse> listenSuccess = PublishSubject.create();
    @Nonnull
    private final PublishSubject<ListenResponse> unListenSuccess = PublishSubject.create();

    @Nonnull
    private final Observable<ResponseOrError<User>> userUpdatesObservable;
    @Nonnull
    private final Context context;

    @Inject
    public UserProfileHalfPresenter(@Nonnull @UiScheduler final Scheduler uiScheduler,
                                    @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                    @Nonnull final ApiService apiService,
                                    @Nonnull @ForActivity Context context) {
        this.context = context;

        userUpdatesObservable = onListenActionClickedSubject
                .throttleFirst(1, TimeUnit.SECONDS)
                .switchMap(new Func1<User, Observable<ResponseOrError<User>>>() {
                    @Override
                    public Observable<ResponseOrError<User>> call(final User user) {
                        final Observable<ResponseOrError<ListenResponse>> request;
                        if (user.isListening()) {
                             request = apiService.unlistenProfile(user.getUsername())
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                     .doOnNext(unListenSuccess::onNext)
                                    .compose(ResponseOrError.<ListenResponse>toResponseOrErrorObservable());
                        } else {
                            request = apiService.listenProfile(user.getUsername())
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .doOnNext(listenSuccess::onNext)
                                    .compose(ResponseOrError.<ListenResponse>toResponseOrErrorObservable());
                        }

                        return request.map(response -> {
                            if (response.isData()) {
                                return ResponseOrError.fromData(user.getListenedProfile());
                            } else {
                                errorSubject.onNext(new Throwable());
                                // On error return current user in order to select/deselect already deselected/selected 'listenProfile' icon
                                return ResponseOrError.fromData(user);
                            }
                        });
                    }
                });
    }

    @Nonnull
    public Observable<ListenResponse> getListenSuccessObservable() {
        return listenSuccess;
    }

    @Nonnull
    public Observable<ListenResponse> getUnListenSuccessObservable() {
        return unListenSuccess;
    }

    public ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull User user) {
        return new ProfileAdapterItems.UserNameAdapterItem(user, moreMenuOptionClickedSubject);
    }

    public ProfileAdapterItems.ThreeIconsAdapterItem getThreeIconsAdapterItem(@Nonnull User user, boolean isNormalUser) {
        return new ProfileAdapterItems.UserThreeIconsAdapterItem(user, isNormalUser,
                actionOnlyForLoggedInUserSubject, onChatIconClickedSubject, onListenActionClickedSubject);
    }

    @Nonnull
    public Observable<ResponseOrError<User>> getUserUpdatesObservable() {
        return userUpdatesObservable;
    }

    public String getShoutsHeaderTitle(User user) {
        return context.getString(R.string.profile_user_shouts, user.getFirstName()).toUpperCase();
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorSubject;
    }

    @Nonnull
    public PublishSubject<Object> getMoreMenuOptionClickedSubject() {
        return moreMenuOptionClickedSubject;
    }

    @Nonnull
    public Observable<Object> getActionOnlyForLoggedInUserObservable() {
        return actionOnlyForLoggedInUserSubject;
    }

    @Nonnull
    public PublishSubject<ChatInfo> getOnChatIconClickedSubject() {
        return onChatIconClickedSubject;
    }
}
