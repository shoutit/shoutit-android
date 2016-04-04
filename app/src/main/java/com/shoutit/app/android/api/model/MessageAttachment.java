package com.shoutit.app.android.api.model;

public class MessageAttachment {

    public static final String ATTACHMENT_TYPE_SHOUT = "shout";
    public static final String ATTACHMENT_TYPE_LOCATION = "location";
    public static final String ATTACHMENT_TYPE_VIDEO = "video";
    public static final String ATTACHMENT_TYPE_IMAGE = "image";

    private final String type;
    private final MessageLocation location;
    private final Shout shout;
    private final MessageImage image;
    private final Video video;

    public MessageAttachment(String type, MessageLocation location, Shout shout, MessageImage image, Video video) {
        this.type = type;
        this.location = location;
        this.shout = shout;
        this.image = image;
        this.video = video;
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

    public MessageImage getImage() {
        return image;
    }

    public Video getVideo() {
        return video;
    }

    public class MessageImage {

        private final String url;

        public MessageImage(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

}