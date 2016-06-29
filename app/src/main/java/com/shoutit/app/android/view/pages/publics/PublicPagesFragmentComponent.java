package com.shoutit.app.android.view.pages.publics;

import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.view.pages.my.MyPagesFragment;

import dagger.Component;


@FragmentScope
@Component(
        dependencies = BaseActivityComponent.class,
        modules = {
                FragmentModule.class,
        }
)
public interface PublicPagesFragmentComponent extends BaseFragmentComponent {

    void inject(PublicPagesFragment fragment);
}



