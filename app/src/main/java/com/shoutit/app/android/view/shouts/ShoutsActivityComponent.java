package com.shoutit.app.android.view.shouts;

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
                ShoutsActivityModule.class
        }
)
public interface ShoutsActivityComponent extends BaseActivityComponent {

    void inject(ShoutsActivity shoutsActivity);

}
