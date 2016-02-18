package com.shoutit.app.android.view.main;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                MainActivityModule.class
        }
)
public interface MainActivityComponent extends BaseActivityComponent {

    void inject(MainActivity activity);

    OnNewDiscoverSelectedListener onNewDiscoverSelectedListener();
}
