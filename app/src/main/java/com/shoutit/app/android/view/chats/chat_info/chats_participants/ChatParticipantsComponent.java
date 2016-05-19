package com.shoutit.app.android.view.chats.chat_info.chats_participants;

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
                ChatParticipantModule.class
        }
)
public interface ChatParticipantsComponent extends BaseActivityComponent {

    void inject(ChatParticipantsActivity activity);

}
