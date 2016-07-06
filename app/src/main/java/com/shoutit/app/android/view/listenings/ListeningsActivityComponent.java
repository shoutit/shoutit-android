package com.shoutit.app.android.view.listenings;

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
                ListeningsActivityModule.class
        }
)
public interface ListeningsActivityComponent extends BaseActivityComponent {

    void inject(ListeningsActivity activity);

    BaseProfileListPresenter profilesListPresenter();

}

