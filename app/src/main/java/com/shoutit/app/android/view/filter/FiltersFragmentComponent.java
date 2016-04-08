package com.shoutit.app.android.view.filter;

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
                FiltersFragmentModule.class
        }
)
public interface FiltersFragmentComponent extends BaseFragmentComponent {

    void inject(FiltersFragment fragment);
}


