package com.shoutit.app.android.api.model;


import javax.annotation.Nonnull;

public class Conversation {

    public static final String ABOUT_SHOUT_TYPE = "about_shout";
    public static final String CHAT_TYPE = "chat";
    public static final String PUBLIC_CHAT_TYPE = "public_chat";


    private final String id;
    private final String type;
    private final int unreadMessagesCount;
    private final Display display;
    private final MiniProfile creator;
    private final long modifiedAt;

    public Conversation(String id, String type, int unreadMessagesCount,
                        Display display, MiniProfile creator, long modifiedAt) {
        this.id = id;
        this.type = type;
        this.unreadMessagesCount = unreadMessagesCount;
        this.display = display;
        this.creator = creator;
        this.modifiedAt = modifiedAt;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public Display getDisplay() {
        return display;
    }

    public boolean isPublicChat() {
        return PUBLIC_CHAT_TYPE.equals(type);
    }

    public boolean isShoutChat() {
        return ABOUT_SHOUT_TYPE.equals(type);
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public MiniProfile getCreator() {
        return creator;
    }

    public Conversation withUpdatedLastMessage(@Nonnull String newMessage, long createdAt) {
        return new Conversation(id, type, unreadMessagesCount + 1,
                display.withUpdatedMessage(newMessage), creator, createdAt);
    }

    public class Display {
        private final String image;
        private final String subTitle;
        private final String title;
        private final String lastMessageSummary;

        public Display(String image, String subTitle, String title, String lastMessageSummary) {
            this.image = image;
            this.subTitle = subTitle;
            this.title = title;
            this.lastMessageSummary = lastMessageSummary;
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

        public String getLastMessageSummary() {
            return lastMessageSummary;
        }

        public Display withUpdatedMessage(@Nonnull String newMessage) {
            return new Display(image, subTitle, title, newMessage);
        }
    }
}