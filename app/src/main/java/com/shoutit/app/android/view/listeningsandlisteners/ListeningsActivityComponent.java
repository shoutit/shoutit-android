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
                ListeningsActivityModule.class
        }
)
public interface ListeningsActivityComponent extends BaseActivityComponent {

    void inject(ListeningsActivity activity);

}

