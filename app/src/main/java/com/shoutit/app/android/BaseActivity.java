package com.shoutit.app.android;

import android.os.Bundle;

import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseActivityComponentProvider;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import javax.annotation.Nonnull;


public abstract class BaseActivity extends RxAppCompatActivity implements BaseActivityComponentProvider {

    private BaseActivityComponent activityComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityComponent = createActivityComponent(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Nonnull
    public BaseActivityComponent getActivityComponent() {
        return activityComponent;
    }
}
