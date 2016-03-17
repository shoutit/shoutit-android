package com.shoutit.app.android.view.search.main;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.search.BaseSearchActivityModule;
import com.shoutit.app.android.view.search.SearchQueryPresenter;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                BaseSearchActivityModule.class
        }
)
public interface MainSearchActivityComponent extends BaseActivityComponent {

    void inject(MainSearchActivity activity);

    SearchQueryPresenter searchQueryPresenter();

}


