package com.shoutit.app.android.view.shout;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.videoconversation.VideoConversationModule;

import dagger.Component;


@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                ShoutActivityModule.class
        }
)
public interface ShoutActivityComponent extends BaseActivityComponent {

    void inject(ShoutActivity activity);
}
