package com.shoutit.app.android.view.pages.my;

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
public interface PagesFragmentComponent extends BaseFragmentComponent {

    void inject(MyPagesFragment fragment);
}



