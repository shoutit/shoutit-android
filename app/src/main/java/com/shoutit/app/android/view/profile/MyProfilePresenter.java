package com.shoutit.app.android.view.profile;

import android.content.Context;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class MyProfilePresenter extends ProfilePresenter {

    public MyProfilePresenter(@Nonnull String userName,
                              @Nonnull ShoutsDao shoutsDao,
                              @Nonnull @ForActivity Context context,
                              @Nonnull UserPreferences userPreferences,
                              @Nonnull @UiScheduler Scheduler scheduler) {
        super(userName, shoutsDao, context, userPreferences, scheduler);
    }

    @Override
    protected ProfileAdapterItems.UserNameAdapterItem getUserNameAdapterItem(@Nonnull User user) {
        return new MyUserNameAdapterItem(user);
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

    public class MyUserNameAdapterItem extends ProfileAdapterItems.UserNameAdapterItem {

        public MyUserNameAdapterItem(@Nonnull User user) {
            super(user);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.UserNameAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.UserNameAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }
    }

    public class MyUserThreeIconsAdapterItem extends ProfileAdapterItems.ThreeIconsAdapterItem {

        public MyUserThreeIconsAdapterItem(@Nonnull User user) {
            super(user);
        }
    }
}
