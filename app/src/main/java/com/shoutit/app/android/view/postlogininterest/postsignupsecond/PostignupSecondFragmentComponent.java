package com.shoutit.app.android.view.postlogininterest.postsignupsecond;

import com.shoutit.app.android.PostSignupPresenterComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = {BaseActivityComponent.class, PostSignupPresenterComponent.class},
        modules = {
                FragmentModule.class,
        }
)
public interface PostignupSecondFragmentComponent extends BaseFragmentComponent {

    void inject(PostSignupSecondFragment fragment);
}



