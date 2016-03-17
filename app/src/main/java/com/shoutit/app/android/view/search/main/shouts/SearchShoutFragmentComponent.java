package com.shoutit.app.android.view.search.main.shouts;

import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.view.search.main.MainSearchActivityComponent;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = MainSearchActivityComponent.class,
        modules = {
                FragmentModule.class,
                SearchShoutsFragmentModule.class
        }
)
public interface SearchShoutFragmentComponent extends BaseFragmentComponent {

    void inject(SearchShoutFragment fragment);
}




