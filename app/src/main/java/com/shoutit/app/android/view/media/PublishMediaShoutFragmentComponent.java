package com.shoutit.app.android.view.media;

import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentScope;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = AppComponent.class
)
public interface PublishMediaShoutFragmentComponent extends BaseFragmentComponent {

    void inject(PublishMediaShoutFragment fragment);
}

