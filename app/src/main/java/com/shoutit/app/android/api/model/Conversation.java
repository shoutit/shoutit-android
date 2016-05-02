package com.shoutit.app.android.api.model;

import java.util.List;

public class Conversation {

    public static final String ABOUT_SHOUT_TYPE = "about_shout";
    public static final String CHAT_TYPE = "chat";


    private final String id;
    private final List<ConversationProfile> profiles;
    private final String type;
    private final Message lastMessage;
    private final AboutShout about;
    private final int unreadMessagesCount;

    public Conversation(String id, List<ConversationProfile> profiles, String type, Message lastMessage, AboutShout about, int unreadMessagesCount) {
        this.id = id;
        this.profiles = profiles;
        this.type = type;
        this.lastMessage = lastMessage;
        this.about = about;
        this.unreadMessagesCount = unreadMessagesCount;
    }

    public String getId() {
        return id;
    }

    public List<ConversationProfile> getProfiles() {
        return profiles;
    }

    public String getType() {
        return type;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public AboutShout getAbout() {
        return about;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }
}