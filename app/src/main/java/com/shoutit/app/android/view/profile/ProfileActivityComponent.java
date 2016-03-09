package com.shoutit.app.android.view.profile;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profile.myprofile.MyProfileActivity;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                ProfileActivityModule.class
        }
)
public interface ProfileActivityComponent extends BaseActivityComponent {

    void inject(ProfileActivity activity);

    void inject(MyProfileActivity activity);

    ProfilePresenter getPresenter();
}

