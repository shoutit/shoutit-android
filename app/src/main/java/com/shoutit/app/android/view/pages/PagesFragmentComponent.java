package com.shoutit.app.android.view.pages;

import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;

import dagger.Component;


@FragmentScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                FragmentModule.class,
        }
)
public interface PagesFragmentComponent extends BaseFragmentComponent {

    void inject(PagesFragment fragment);
}



