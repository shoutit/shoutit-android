package com.shoutit.app.android.dagger;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = BaseActivityComponent.class,
        modules = {
                FragmentModule.class
        }
)
public interface BaseFragmentComponent {

}
