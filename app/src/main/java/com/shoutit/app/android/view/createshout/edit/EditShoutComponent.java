package com.shoutit.app.android.view.createshout.edit;

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
                EditShoutActivityModule.class
        }
)
public interface EditShoutComponent extends BaseActivityComponent {

    void inject(EditShoutActivity activity);

}
