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

public class ListeningsActivity extends ListeningsAndListenersActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, ListeningsActivity.class);
    }

    @Override
    String getToolbarTitle() {
        return getString(R.string.listenings_ab_title);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ListeningsActivityComponent component = DaggerListeningsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .listeningsActivityModule(new ListeningsActivityModule())
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
