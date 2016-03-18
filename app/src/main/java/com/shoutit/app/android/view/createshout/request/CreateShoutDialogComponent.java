package com.shoutit.app.android.view.createshout.request;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.view.createshout.CreateShoutDialogFragment;
import com.shoutit.app.android.view.media.NativeCameraActivity;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
        }
)
public interface CreateShoutDialogComponent extends BaseActivityComponent {

    void inject(CreateShoutDialogFragment activity);

}