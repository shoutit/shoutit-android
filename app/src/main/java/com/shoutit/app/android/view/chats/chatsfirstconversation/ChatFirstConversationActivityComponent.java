package com.shoutit.app.android.view.chats.chatsfirstconversation;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.chats.ChatActivity;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                ChatsFirstConversationActivityModule.class
        }
)
public interface ChatFirstConversationActivityComponent extends BaseActivityComponent {

    void inject(ChatFirstConversationActivity activity);

}

