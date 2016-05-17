package com.shoutit.app.android.api.model;

import java.util.List;

public class Conversation {

    public static final String ABOUT_SHOUT_TYPE = "about_shout";
    public static final String CHAT_TYPE = "chat";
    public static final String PUBLIC_CHAT_TYPE = "public_chat";


    private final String id;
    private final List<ConversationProfile> profiles;
    private final String type;
    private final Message lastMessage;
    private final AboutShout about;
    private final int unreadMessagesCount;
    private final List<String> blocked;
    private final List<String> admins;
    private final String subject;
    private final String icon;

    public Conversation(String id, List<ConversationProfile> profiles, String type, Message lastMessage, AboutShout about, int unreadMessagesCount, List<String> blocked, List<String> admins, String subject, String icon) {
        this.id = id;
        this.profiles = profiles;
        this.type = type;
        this.lastMessage = lastMessage;
        this.about = about;
        this.unreadMessagesCount = unreadMessagesCount;
        this.blocked = blocked;
        this.admins = admins;
        this.subject = subject;
        this.icon = icon;
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

    public List<String> getBlocked() {
        return blocked;
    }

    public String getSubject() {
        return subject;
    }

    public String getIcon() {
        return icon;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public boolean isPublicChat() {
        return PUBLIC_CHAT_TYPE.equals(type);
    }
}