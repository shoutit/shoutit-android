package com.shoutit.app.android.view.postlogininterest;

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
                PostLoginActivityModule.class
        }
)
public interface PostLoginActivityComponent extends BaseActivityComponent {

    void inject(PostSignUpActivity activity);

}