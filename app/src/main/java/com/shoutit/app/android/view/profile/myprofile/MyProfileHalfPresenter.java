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

    @Inject
    public MyProfileHalfPresenter(@ForActivity Context context) {
        this.context = context;
    }

    public ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull User user) {
        return new ProfileAdapterItems.MyUserNameAdapterItem(user, editProfileClickObserver,
                notificationsClickObserver, verifyAccountClickObserver);
    }

    public ProfileAdapterItems.MyUserThreeIconsAdapterItem getThreeIconsAdapterItem(@Nonnull User user) {
        return new ProfileAdapterItems.MyUserThreeIconsAdapterItem(user);
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
}
