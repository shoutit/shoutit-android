package com.shoutit.app.android.camera2;

import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.view.settings.account.AccountFragment;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = AppComponent.class
)
public interface PublishMediaShoutFragmentComponent extends BaseFragmentComponent {

    void inject(PublishMediaShoutFragment fragment);
}

