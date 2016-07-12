package com.shoutit.app.android.view.profile.page.userprofile;

import android.content.Context;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.profile.page.ChatInfo;
import com.shoutit.app.android.view.profile.page.ProfileAdapterItems;

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
    private final PublishSubject<Page> onListenActionClickedSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();

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

        userUpdatesObservable = Observable.empty();
    }

    @Nonnull
    public Observable<String> getListenSuccessObservable() {
        return listenSuccess;
    }

    @Nonnull
    public Observable<String> getUnListenSuccessObservable() {
        return unListenSuccess;
    }

    public ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull Page user) {
        return new ProfileAdapterItems.UserNameAdapterItem(user, moreMenuOptionClickedSubject);
    }

    public ProfileAdapterItems.ThreeIconsAdapterItem getThreeIconsAdapterItem(@Nonnull Page user, boolean isNormalUser) {
        return new ProfileAdapterItems.UserThreeIconsAdapterItem(user, isNormalUser,
                actionOnlyForLoggedInUserSubject, onChatIconClickedSubject, onListenActionClickedSubject);
    }

    @Nonnull
    public Observable<ResponseOrError<User>> getUserUpdatesObservable() {
        return userUpdatesObservable;
    }

    public String getShoutsHeaderTitle(Page user) {
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
