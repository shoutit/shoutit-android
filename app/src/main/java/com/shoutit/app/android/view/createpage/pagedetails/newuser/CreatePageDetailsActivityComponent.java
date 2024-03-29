package com.shoutit.app.android.view.createpage.pagedetails.newuser;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {ActivityModule.class, CreatePageDetailsModule.class}
)
public interface CreatePageDetailsActivityComponent extends BaseActivityComponent {

    void inject(CreatePageDetailsActivity activity);
}
