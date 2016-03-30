package com.shoutit.app.android.api.model;


import java.util.List;

public class ConversationsResponse {

    private final String next;
    private final List<Conversation> results;

    public ConversationsResponse(String next, List<Conversation> results) {
        this.next = next;
        this.results = results;
    }

    public static class Conversation {

        public static final String ABOUT_SHOUT_TYPE = "about_shout";
        public static final String CHAT_TYPE = "chat";


        private final String id;
        private final List<ConversationProfile> profiles;
        private final String type;
        private final Message lastMessage;
        private final AboutShout about;

        public Conversation(String id, List<ConversationProfile> profiles, String type, Message lastMessage, AboutShout about) {
            this.id = id;
            this.profiles = profiles;
            this.type = type;
            this.lastMessage = lastMessage;
            this.about = about;
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
    }

    public static class ConversationProfile {
        private final String id;
        private final String firstName;
        private final String username;

        public ConversationProfile(String id, String firstName, String username) {
            this.id = id;
            this.firstName = firstName;
            this.username = username;
        }

        public String getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class Message {

        private final String id;
        private final String text;
        private final List<MessageAttachment> attachments;
        private final long createdAt;


        public Message(String id, String text, List<MessageAttachment> attachments, long createdAt) {
            this.id = id;
            this.text = text;
            this.attachments = attachments;
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public List<MessageAttachment> getAttachments() {
            return attachments;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }

    public static class MessageAttachment {

        private final String type;
        private final MessageLocation location;

        public MessageAttachment(String type, MessageLocation location) {
            this.type = type;
            this.location = location;
        }

        public class MessageLocation {
            private final double latitude;
            private final double longitude;

            public MessageLocation(double latitude, double longitude) {
                this.latitude = latitude;
                this.longitude = longitude;
            }

            public double getLatitude() {
                return latitude;
            }

            public double getLongitude() {
                return longitude;
            }
        }
    }

    public String getNext() {
        return next;
    }

    public List<Conversation> getResults() {
        return results;
    }

    public static class AboutShout {

        private final String title;

        public AboutShout(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}
