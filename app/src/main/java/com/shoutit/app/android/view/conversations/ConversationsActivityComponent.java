package com.shoutit.app.android.view.conversations;

import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.view.main.MainActivityComponent;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = MainActivityComponent.class,
        modules = {
                FragmentModule.class,
        }
)
public interface ConversationsActivityComponent extends BaseFragmentComponent {

    void inject(ConverstationsFragment activity);

}
