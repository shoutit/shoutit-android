package com.shoutit.app.android.view.settings.account;

import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;

import dagger.Component;


@FragmentScope
@Component(
        dependencies = BaseActivityComponent.class,
        modules = {
                FragmentModule.class,
        }
)
public interface ChangePasswordFragmentComponent extends BaseFragmentComponent {

    void inject(ChangePasswordFragment fragment);
}

