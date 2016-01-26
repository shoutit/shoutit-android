package com.shoutit.app.android.dagger;

import dagger.Component;

@ActivityScope
@Component(
        modules = {
                FragmentModule.class
        }
)
public interface BaseFragmentComponent {

}
