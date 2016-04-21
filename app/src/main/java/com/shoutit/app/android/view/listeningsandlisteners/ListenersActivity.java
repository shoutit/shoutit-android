package com.shoutit.app.android.view.listeningsandlisteners;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ListenersActivity extends ListeningsAndListenersActivity {

    private static final String EXTRA_USER_NAME = "user_name";

    public static Intent newIntent(Context context, @Nonnull String userName) {
        return new Intent(context, ListenersActivity.class)
                .putExtra(EXTRA_USER_NAME, userName);
    }

    @Override
    String getToolbarTitle() {
        return getString(R.string.listers_ab_title);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String userName = getIntent().getStringExtra(EXTRA_USER_NAME);

        final ListenersActivityComponent component = DaggerListenersActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .listenersActivityModule(new ListenersActivityModule(userName))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
