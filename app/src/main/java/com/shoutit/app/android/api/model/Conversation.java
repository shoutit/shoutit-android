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
    private final DisplayData display;

    public Conversation(String id, List<ConversationProfile> profiles, String type,
                        Message lastMessage, AboutShout about, int unreadMessagesCount, DisplayData display) {
        this.id = id;
        this.profiles = profiles;
        this.type = type;
        this.lastMessage = lastMessage;
        this.about = about;
        this.unreadMessagesCount = unreadMessagesCount;
        this.display = display;
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

    public DisplayData getDisplay() {
        return display;
    }

    public boolean isPublicChat() {
        return PUBLIC_CHAT_TYPE.equals(type);
    }

    public class DisplayData {
        private final String image;
        private final String subTitle;
        private final String title;

        public DisplayData(String image, String subTitle, String title) {
            this.image = image;
            this.subTitle = subTitle;
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public String getTitle() {
            return title;
        }
    }
}