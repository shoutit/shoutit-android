package com.shoutit.app.android.view.invitefriends.suggestionsusers;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import dagger.Component;


@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                UserSuggestionActivityModule.class
        }
)
public interface UserSuggestionActivityComponent extends BaseActivityComponent {

    void inject(UserSuggestionActivity activity);

    BaseProfileListPresenter profilesListPresenter();
}

