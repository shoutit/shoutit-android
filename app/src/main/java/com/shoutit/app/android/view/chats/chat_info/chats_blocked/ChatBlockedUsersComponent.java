package com.shoutit.app.android.view.chats.chat_info.chats_blocked;

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
                ChatBlockedUsersModule.class
        }
)
public interface ChatBlockedUsersComponent extends BaseActivityComponent {

    void inject(ChatBlockedUsersActivity activity);

}
