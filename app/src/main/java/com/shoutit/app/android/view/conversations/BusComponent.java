package com.shoutit.app.android.view.conversations;

import com.shoutit.app.android.view.chats.LocalMessageBus;

public interface BusComponent {

    LocalMessageBus localMessageBus();

    RefreshConversationBus refreshConversationBus();

}
