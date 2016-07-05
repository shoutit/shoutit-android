package com.shoutit.app.android.view.pages.my;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.inject.Named;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                SelectListenersActivityModule.class
        }
)
public interface SelectListenersActivityComponent extends BaseActivityComponent {

    void inject(SelectListenersActivity activity);

    BaseProfileListPresenter profilesWithoutPagesListPresenter();
}

