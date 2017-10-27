package com.shoutit.app.android.view.profile.page.myprofile;

import android.content.Context;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.BusinessVerificationResponse;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BusinessVerificationDaos;
import com.shoutit.app.android.view.profile.page.ProfileAdapterItems;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class MyProfileHalfPresenter {

    @Nonnull
    private final PublishSubject<String> editProfileClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> notificationsClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Page> verifyAccountClickObserver = PublishSubject.create();
    private final Context context;
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

    public ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull Page page,
                                                                      @Nonnull Observable<Integer> notificationsUnreadObservable) {
        return new ProfileAdapterItems.MyUserNameAdapterItem(page, editProfileClickObserver,
                notificationsClickObserver, verifyAccountClickObserver, notificationsUnreadObservable,
                shouldShowProfileBadge(page), pageVerificationObservable);
    }

    public ProfileAdapterItems.MyProfileThreeIconsAdapterItem getThreeIconsAdapterItem(@Nonnull Page user) {
        return new ProfileAdapterItems.MyProfileThreeIconsAdapterItem(
                user, listeningsClickObserver, interestsClickObserver, listenersClickObserver);
    }

    private boolean shouldShowProfileBadge(@Nonnull Page user) {
        return false;
    }

    public String getShoutsHeaderTitle() {
        return context.getString(R.string.profile_my_shouts);
    }

    @Nonnull
    public Observable<String> getEditProfileClickObservable() {
        return editProfileClickObserver;
    }

    @Nonnull
    public Observable<Object> getNotificationsClickObservable() {
        return notificationsClickObserver;
    }

    @Nonnull
    public Observable<Page> getVerifyAccountClickObservable() {
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
