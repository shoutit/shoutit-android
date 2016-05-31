package com.shoutit.app.android.view.postlogininterest.postsignupsecond;

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
                PostSignupSecondActivityModule.class
        }
)
public interface PostSignupSecondActivityComponent extends BaseActivityComponent, PostSignupPresenterComponent {

    void inject(PostSignupSecondActivity activity);

}