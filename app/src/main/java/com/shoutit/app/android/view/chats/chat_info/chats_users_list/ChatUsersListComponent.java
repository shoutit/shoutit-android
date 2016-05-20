package com.shoutit.app.android.view.chats.chat_info.chats_users_list;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.ChatUsersListModule;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.chats_blocked.ChatBlockedUsersActivity;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.chats_select.ChatSelectUsersActivity;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                ChatUsersListModule.class
        }
)
public interface ChatUsersListComponent extends BaseActivityComponent {

    void inject(ChatBlockedUsersActivity activity);
    void inject(ChatSelectUsersActivity activity);

}
