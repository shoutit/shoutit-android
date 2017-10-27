package com.shoutit.app.android.view.profile.user.editprofile;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {ActivityModule.class, EditProfileActivityModule.class}
)
public interface EditProfileActivityComponent extends BaseActivityComponent {

    void inject(EditProfileActivity activity);
}
