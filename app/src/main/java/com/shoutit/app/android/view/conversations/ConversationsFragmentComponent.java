package com.shoutit.app.android.view.conversations;

import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.view.main.MainActivityComponent;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = {BaseActivityComponent.class, BusComponent.class},
        modules = {
                FragmentModule.class,
                ConverstationsFragmentModule.class
        }
)
public interface ConversationsFragmentComponent extends BaseFragmentComponent {

    void inject(ConversationsFragment fragment);

}

