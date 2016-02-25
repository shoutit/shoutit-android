package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.functions.Func2;

public class MyProfilePresenter extends ProfilePresenter {

    @Nonnull
    private final UserPreferences userPreferences;

    public MyProfilePresenter(@Nonnull String userName,
                              @Nonnull ShoutsDao shoutsDao,
                              @Nonnull @ForActivity Context context,
                              @Nonnull Resources resources,
                              @Nonnull UserPreferences userPreferences) {
        super(userName, shoutsDao, context, resources);
        this.userPreferences = userPreferences;
    }

    @NonNull
    @Override
    protected Func2<User, List<BaseAdapterItem>, List<BaseAdapterItem>> combineAdapterItems() {
        return new Func2<User, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
            @Override
            public List<BaseAdapterItem> call(User user, List<BaseAdapterItem> shouts) {
                final ImmutableList.Builder<Object> builder = ImmutableList.builder();

                builder.add(new MyUserAdapterItem(user));
                builder.add()
            }
        };
    }

    @Nonnull
    @Override
    protected Observable<ResponseOrError<User>> getUserObservable() {
        return userPreferences.getUserObservable()
                .compose(ResponseOrError.<User>toResponseOrErrorObservable());
    }

    public class MyUserAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final User user;

        public MyUserAdapterItem(@Nonnull User user) {
            this.user = user;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof UserAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof UserAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }
    }
}
