package com.shoutit.app.android.view.chats.chat_shouts;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import dagger.Component;


@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                ChatShoutsActivityModule.class
        }
)
public interface ChatShoutsActivityComponent extends BaseActivityComponent {

    void inject(ChatShoutsActivity activity);

}
