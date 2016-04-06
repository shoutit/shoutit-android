package com.shoutit.app.android.api.model;

public class ConversationProfile {

        public static final String TYPE_USER = "user";

        private final String id;
        private final String firstName;
        private final String username;
        private final String type;
        private final String image;

        public ConversationProfile(String id, String firstName, String username, String type, String image) {
            this.id = id;
            this.firstName = firstName;
            this.username = username;
            this.type = type;
            this.image = image;
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

        public String getType() {
            return type;
        }

        public String getImage() {
            return image;
        }
    }