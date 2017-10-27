package com.shoutit.app.android.view.profile.user.myprofile;

import android.content.Context;
import android.text.TextUtils;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.BusinessVerificationResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BusinessVerificationDaos;
import com.shoutit.app.android.view.profile.user.ProfileAdapterItems;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class MyProfileHalfPresenter {

    @Nonnull
    private final PublishSubject<Object> editProfileClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> notificationsClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<User> verifyAccountClickObserver = PublishSubject.create();
    private final Context context;
    private final UserPreferences userPreferences;
    @Nonnull
    private final PublishSubject<Object> listeningsClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> interestsClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> listenersClickObserver = PublishSubject.create();

    @Nonnull
    private final Observable<BusinessVerificationResponse> pageVerificationObservable;

    @Inject
    public MyProfileHalfPresenter(@ForActivity Context context,
                                  UserPreferences userPreferences,
                                  BusinessVerificationDaos businessVerificationDaos,
                                  @UiScheduler Scheduler uiScheduler) {
        this.context = context;
        this.userPreferences = userPreferences;

        //noinspection ConstantConditions
        final BaseProfile currentProfile = userPreferences.getUserOrPage();
        if (currentProfile == null || currentProfile.isUser()) {
            pageVerificationObservable = Observable.empty();
        } else {
            final String myUsername = currentProfile.getUsername();
            pageVerificationObservable = businessVerificationDaos
                    .getDao(myUsername)
                    .getVerificationObservable()
                    .observeOn(uiScheduler)
                    .compose(ResponseOrError.onlySuccess());
        }
    }

    public ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull User user,
                                                                      @Nonnull Observable<Integer> notificationsUnreadObservable) {
        return new ProfileAdapterItems.MyUserNameAdapterItem(user, editProfileClickObserver,
                notificationsClickObserver, verifyAccountClickObserver, notificationsUnreadObservable,
                shouldShowProfileBadge(user), pageVerificationObservable);
    }

    public ProfileAdapterItems.MyProfileThreeIconsAdapterItem getThreeIconsAdapterItem(@Nonnull User user) {
        return new ProfileAdapterItems.MyProfileThreeIconsAdapterItem(
                user, listeningsClickObserver, interestsClickObserver, listenersClickObserver);
    }

    private boolean shouldShowProfileBadge(@Nonnull User user) {
        return !userPreferences.wasProfileAlertAlreadyDisplayed() &&
                (TextUtils.isEmpty(user.getImage()) ||
                        TextUtils.isEmpty(user.getGender()) ||
                        user.getBirthday() == null);
    }

    public String getShoutsHeaderTitle() {
        return context.getString(R.string.profile_my_shouts);
    }

    @Nonnull
    public Observable<Object> getEditProfileClickObservable() {
        return editProfileClickObserver;
    }

    @Nonnull
    public Observable<Object> getNotificationsClickObservable() {
        return notificationsClickObserver;
    }

    @Nonnull
    public Observable<User> getVerifyAccountClickObservable() {
        return verifyAccountClickObserver;
    }

    @Nonnull
    public Observable<Object> getListeningsClickObservable() {
        return listeningsClickObserver;
    }

    @Nonnull
    public Observable<Object> getInterestsClickObservable() {
        return interestsClickObserver;
    }

    @Nonnull
    public Observable<Object> getListenersClickObservable() {
        return listenersClickObserver;
    }
}
