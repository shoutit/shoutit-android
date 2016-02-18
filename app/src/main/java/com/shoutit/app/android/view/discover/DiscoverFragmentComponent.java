package com.shoutit.app.android.view.discover;

import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.view.main.MainActivityComponent;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = MainActivityComponent.class,
        modules = {
                FragmentModule.class,
                DiscoverFragmentModule.class
        }
)
public interface DiscoverFragmentComponent extends BaseFragmentComponent {

    void inject(DiscoverFragment fragment);
}
