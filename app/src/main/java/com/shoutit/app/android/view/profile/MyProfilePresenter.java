package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.view.shout.ShoutAdapterItems;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func2;

public class MyProfilePresenter extends ProfilePresenter {

    public MyProfilePresenter(@Nonnull String userName,
                              @Nonnull ShoutsDao shoutsDao,
                              @Nonnull @ForActivity Context context,
                              @Nonnull UserPreferences userPreferences,
                              @Nonnull @UiScheduler Scheduler scheduler) {
        super(userName, shoutsDao, context, userPreferences, scheduler);
    }

    @Override
    protected ProfileAdpaterItems.UserNameAdapterItem getUserNameAdapterItem(@Nonnull User user) {
        return new MyUserNameAdapterItem(user);
    }

    @Nonnull
    @Override
    protected Observable<ResponseOrError<User>> getUserObservable() {
        return userPreferences.getUserObservable()
                .compose(ResponseOrError.<User>toResponseOrErrorObservable());
    }

    @Override
    protected String getSectionHeaderTitle(String profileType, User user) {
        return
    }

    @Override
    protected String getShoutsHeaderTitle(User user) {
        return context.getString(R.string.) ?
    }

    public class MyUserNameAdapterItem extends ProfileAdpaterItems.UserNameAdapterItem {

        public MyUserNameAdapterItem(@Nonnull User user) {
            super(user);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdpaterItems.UserNameAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdpaterItems.UserNameAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }
    }
}
