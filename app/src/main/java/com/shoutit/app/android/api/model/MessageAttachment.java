package com.shoutit.app.android.api.model;

public class MessageAttachment {

        public static final String ATTACHMENT_TYPE_SHOUT = "shout";
        public static final String ATTACHMENT_TYPE_LOCATION = "location";
        public static final String ATTACHMENT_TYPE_VIDEO = "video";
        public static final String ATTACHMENT_TYPE_IMAGE = "image";

        private final String type;
        private final MessageLocation location;
        private final Shout shout;

        public MessageAttachment(String type, MessageLocation location, Shout shout) {
            this.type = type;
            this.location = location;
            this.shout = shout;
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

        public String getType() {
            return type;
        }

        public MessageLocation getLocation() {
            return location;
        }

        public Shout getShout() {
            return shout;
        }
    }