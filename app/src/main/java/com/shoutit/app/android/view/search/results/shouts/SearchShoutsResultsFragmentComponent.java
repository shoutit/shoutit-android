package com.shoutit.app.android.view.search.results.shouts;

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
                SearchShoutsResultsFragmentModule.class
        }
)
public interface SearchShoutsResultsFragmentComponent extends BaseFragmentComponent {

    void inject(SearchShoutsResultsFragment fragment);
}


