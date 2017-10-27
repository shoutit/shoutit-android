package com.shoutit.app.android.view.profile.tagprofile;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profile.user.ProfilePresenter;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                TagProfileActivityModule.class
        }
)
public interface TagProfileActivityComponent extends BaseActivityComponent {

    void inject(TagProfileActivity activity);

    ProfilePresenter getPresenter();
}
