package com.shoutit.app.android.view.search.results.profiles;

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
                SearchProfilesResultsActivityModule.class
        }
)
public interface SearchProfilesResultsActivityComponent extends BaseActivityComponent {

    void inject(SearchProfilesResultsActivity activity);

}

