package com.shoutit.app.android.view.listeningsandlisteners;

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
                ListenersActivityModule.class
        }
)
public interface ListenersActivityComponent extends BaseActivityComponent {

    void inject(ListenersActivity activity);

}


