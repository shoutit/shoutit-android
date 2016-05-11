package com.shoutit.app.android.view.chooseprofile;

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
                SelectProfileActivityModule.class
        }
)
public interface SelectProfileActivityComponent extends BaseActivityComponent {

    void inject(SelectProfileActivity activity);

    SelectProfilePresenter selectProfilePresenter();

}

