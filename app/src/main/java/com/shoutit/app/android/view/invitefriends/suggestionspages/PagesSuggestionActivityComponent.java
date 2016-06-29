package com.shoutit.app.android.view.invitefriends.suggestionspages;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import dagger.Component;


@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                PagesSuggestionActivityModule.class
        }
)
public interface PagesSuggestionActivityComponent extends BaseActivityComponent {

    void inject(PagesSuggestionActivity activity);

    ProfilesListPresenter profilesListPresenter();
}

