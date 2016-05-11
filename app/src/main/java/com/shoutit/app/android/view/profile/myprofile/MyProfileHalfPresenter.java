package com.shoutit.app.android.view.profile.myprofile;

import android.content.Context;

import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.profile.ProfileAdapterItems;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public class MyProfileHalfPresenter {

    @Nonnull
    private final PublishSubject<Object> editProfileClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> notificationsClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> verifyAccountClickObserver = PublishSubject.create();
    private final Context context;
    @Nonnull
    private final PublishSubject<Object> listeningsClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> interestsClickObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> listenersClickObserver = PublishSubject.create();

    @Inject
    public MyProfileHalfPresenter(@ForActivity Context context) {
        this.context = context;
    }

    public ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull User user,
                                                                      @Nonnull Observable<Integer> notificationsUnreadObservable) {
        return new ProfileAdapterItems.MyUserNameAdapterItem(user, editProfileClickObserver,
                notificationsClickObserver, verifyAccountClickObserver, notificationsUnreadObservable);
    }

    public ProfileAdapterItems.MyProfileThreeIconsAdapterItem getThreeIconsAdapterItem(@Nonnull User user) {
        return new ProfileAdapterItems.MyProfileThreeIconsAdapterItem(
                user, listeningsClickObserver, interestsClickObserver, listenersClickObserver);
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
    public Observable<Object> getVerifyAccountClickObservable() {
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
