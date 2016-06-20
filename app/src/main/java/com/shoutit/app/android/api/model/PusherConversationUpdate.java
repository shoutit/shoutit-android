package com.shoutit.app.android.api.model;

public class PusherConversationUpdate {

    private final String id;
    private final Display display;
    private final int unreadMessagesCount;
    private final String type;
    private final long modifiedAt;

    public PusherConversationUpdate(String id, Display display, int unreadMessagesCount, String type, long modifiedAt) {
        this.id = id;
        this.display = display;
        this.unreadMessagesCount = unreadMessagesCount;
        this.type = type;
        this.modifiedAt = modifiedAt;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return display.getImage();
    }

    public String getSubTitle() {
        return display.getSubTitle();
    }

    public String getTitle() {
        return display.getTitle();
    }

    public String getLastMessageSummary() {
        return display.getLastMessageSummary();
    }

    public Display getDisplay() {
        return display;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public String getType() {
        return type;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    private static class Display {

        private final String title;
        private final String subTitle;
        private final String lastMessageSummary;
        private final String image;

        public Display(String title, String subTitle, String lastMessageSummary, String image) {
            this.title = title;
            this.subTitle = subTitle;
            this.lastMessageSummary = lastMessageSummary;
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public String getLastMessageSummary() {
            return lastMessageSummary;
        }

        public String getImage() {
            return image;
        }
    }
}
