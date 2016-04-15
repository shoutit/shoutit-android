package com.shoutit.app.android.view.chats.chats_next_conversation;

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
                ChatsActivityModule.class
        }
)
public interface ChatActivityComponent extends BaseActivityComponent {

    void inject(ChatActivity activity);

}

