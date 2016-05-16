package com.shoutit.app.android.view.chooseprofile;

import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;

import dagger.Component;


@FragmentScope
@Component(
        dependencies = SelectProfileActivityComponent.class,
        modules = {
                FragmentModule.class,
        }
)
public interface SelectProfileFragmentComponent extends BaseFragmentComponent {

    void inject(SelectProfileFragment fragment);
}


