package com.shoutit.app.android.view.search.subsearch;

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
                SubSearchActivityModule.class
        }
)
public interface SubSearchActivityComponent extends BaseActivityComponent {

    void inject(SubSearchActivity activity);
}



