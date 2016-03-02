package com.shoutit.app.android.view.profile.myprofile;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.view.profile.ProfileAdapterItems;
import com.shoutit.app.android.view.profile.ProfilePresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class MyProfilePresenter extends ProfilePresenter {

    @Nonnull
    private PublishSubject<Object> editProfileClickObserver = PublishSubject.create();
    @Nonnull
    private PublishSubject<Object> notificationsClickObserver = PublishSubject.create();

    public MyProfilePresenter(@Nonnull String userName,
                              @Nonnull ShoutsDao shoutsDao,
                              @Nonnull @ForActivity Context context,
                              @Nonnull UserPreferences userPreferences,
                              @Nonnull @UiScheduler Scheduler scheduler,
                              @Nonnull @NetworkScheduler Scheduler networkScheduler,
                              @Nonnull ApiService apiService) {
        super(userName, shoutsDao, context, userPreferences, true, scheduler, networkScheduler, apiService);
        initPresenter();
    }

    @Override
    protected ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull User user) {
        return new MyUserNameAdapterItem(user, editProfileClickObserver, notificationsClickObserver);
    }

    @Override
    protected ProfileAdapterItems.ThreeIconsAdapterItem getThreeIconsAdapterItem(@Nonnull User user) {
        return new MyUserThreeIconsAdapterItem(user);
    }

    @Nonnull
    @Override
    protected Observable<ResponseOrError<User>> getUserObservable() {
        return userPreferences.getUserObservable()
                .compose(ResponseOrError.<User>toResponseOrErrorObservable());
    }

    @Override
    protected String getShoutsHeaderTitle(User user) {
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

    public class MyUserNameAdapterItem extends ProfileAdapterItems.NameAdapterItem {

        @NonNull
        private final Observer<Object> editProfileClickObserver;
        @Nonnull
        private final Observer<Object> notificationsClickObserver;

        public MyUserNameAdapterItem(@Nonnull User user, @NonNull Observer<Object> editProfileClickObserver,
                                     @Nonnull Observer<Object> notificationsClickObserver) {
            super(user);
            this.editProfileClickObserver = editProfileClickObserver;
            this.notificationsClickObserver = notificationsClickObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.NameAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.NameAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }

        public void onEditProfileClicked() {
            editProfileClickObserver.onNext(null);
        }

        public void onShowNotificationClicked() {
            notificationsClickObserver.onNext(null);
        }
    }

    public class MyUserThreeIconsAdapterItem extends ProfileAdapterItems.ThreeIconsAdapterItem {

        public MyUserThreeIconsAdapterItem(@Nonnull User user) {
            super(user);
        }
    }
}
