package com.shoutit.app.android.view.shouts.selectshout;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class
        }
)
public interface SelectShoutsActivityComponent extends BaseActivityComponent {

    void inject(SelectShoutActivity shoutsActivity);

}
