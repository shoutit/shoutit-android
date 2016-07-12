package com.shoutit.app.android.view.profile.page.edit;

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
                EditPageActivityModule.class
        }
)
public interface EditPageActivityComponent extends BaseActivityComponent {

    void inject(EditPageActivity activity);

}