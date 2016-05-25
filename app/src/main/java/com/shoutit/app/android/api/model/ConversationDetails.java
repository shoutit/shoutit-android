package com.shoutit.app.android.api.model;

import java.util.List;

public class ConversationDetails {

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
    private final MiniProfile creator;
    private final long createdAt;
    private final Display display;
    private final AttatchmentCount attachmentsCount;

    public ConversationDetails(String id, List<ConversationProfile> profiles, String type,
                               Message lastMessage, AboutShout about, int unreadMessagesCount,
                               List<String> blocked, List<String> admins, MiniProfile creator,
                               long createdAt, Display display, AttatchmentCount attachmentsCount) {
        this.id = id;
        this.profiles = profiles;
        this.type = type;
        this.lastMessage = lastMessage;
        this.about = about;
        this.unreadMessagesCount = unreadMessagesCount;
        this.blocked = blocked;
        this.admins = admins;
        this.creator = creator;
        this.createdAt = createdAt;
        this.display = display;
        this.attachmentsCount = attachmentsCount;
    }

    public static String getAboutShoutType() {
        return ABOUT_SHOUT_TYPE;
    }

    public List<String> getBlocked() {
        return blocked;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public MiniProfile getCreator() {
        return creator;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public AttatchmentCount getAttachmentsCount() {
        return attachmentsCount;
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

    public Display getDisplay() {
        return display;
    }

    public boolean isPublicChat() {
        return PUBLIC_CHAT_TYPE.equals(type);
    }

    public boolean isShoutChat() {
        return ABOUT_SHOUT_TYPE.equals(type);
    }

    public static class Display {
        private final String image;
        private final String subTitle;
        private final String title;

        public Display(String image, String subTitle, String title) {
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

    public static class AttatchmentCount {

        private final int profile;
        private final int media;
        private final int shout;
        private final int location;

        public AttatchmentCount(int profile, int media, int shout, int location) {
            this.profile = profile;
            this.media = media;
            this.shout = shout;
            this.location = location;
        }

        public int getProfile() {
            return profile;
        }

        public int getMedia() {
            return media;
        }

        public int getShout() {
            return shout;
        }

        public int getLocation() {
            return location;
        }
    }

}