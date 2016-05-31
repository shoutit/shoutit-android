package com.shoutit.app.android.view.invitefriends;

import com.shoutit.app.android.PostSignupPresenterComponent;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.postlogininterest.postsignupsecond.PostSignupSecondActivityModule;

import dagger.Component;


@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                PostSignupSecondActivityModule.class
        }
)
public interface UserSuggestionsActivityComponent extends BaseActivityComponent, PostSignupPresenterComponent {

    void inject(UserSuggestionActivity activity);

}

