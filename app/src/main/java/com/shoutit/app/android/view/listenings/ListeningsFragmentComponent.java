package com.shoutit.app.android.view.listenings;

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
                ListeningsFragmentModule.class
        }
)
public interface ListeningsFragmentComponent extends BaseFragmentComponent {

    void inject(ListeningsFragment fragment);
}

