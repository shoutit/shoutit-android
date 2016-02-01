package com.shoutit.app.android.view.signin.register;

import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.view.signin.LoginActivityComponent;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = LoginActivityComponent.class,
        modules = {
                FragmentModule.class
        }
)
public interface RegisterFragmentComponent extends BaseFragmentComponent {

    void inject(RegisterFragment registerFragment);

}
