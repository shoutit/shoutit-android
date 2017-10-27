package com.shoutit.app.android;

import android.os.Bundle;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.dagger.DaggerBaseDaggerActivityComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseDaggerActivity extends BaseActivity {

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final BaseDaggerActivityComponent component = DaggerBaseDaggerActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();

        injectComponent(component);

        return component;
    }

    protected abstract void injectComponent(BaseDaggerActivityComponent component);
}
