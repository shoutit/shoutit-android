package com.shoutit.app.android.view.chats.chatsfirstconversation;

import com.shoutit.app.android.view.chats.Listener;

public interface FirstConversationListener extends Listener {

    void showDeleteMenu(boolean show);

    void showChatInfoMenu(boolean show);
}
