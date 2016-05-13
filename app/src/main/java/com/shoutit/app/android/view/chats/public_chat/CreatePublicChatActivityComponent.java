package com.shoutit.app.android.view.chats.public_chat;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.chats.ChatActivity;
import com.shoutit.app.android.view.chats.ChatsActivityModule;
import com.shoutit.app.android.view.createshout.request.CreateRequestActivity;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class
        }
)
public interface CreatePublicChatActivityComponent extends BaseActivityComponent {

    void inject(CreateRequestActivity activity);

}

