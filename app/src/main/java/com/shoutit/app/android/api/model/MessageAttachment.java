package com.shoutit.app.android.api.model;

import java.util.List;

public class MessageAttachment {

    public static final String ATTACHMENT_TYPE_SHOUT = "shout";
    public static final String ATTACHMENT_TYPE_LOCATION = "location";
    public static final String ATTACHMENT_TYPE_MEDIA = "media";

    private final String type;
    private final MessageLocation location;
    private final Shout shout;
    private final List<String> images;
    private final List<Video> videos;

    public MessageAttachment(String type, MessageLocation location, Shout shout, List<String> image, List<Video> video) {
        this.type = type;
        this.location = location;
        this.shout = shout;
        this.images = image;
        this.videos = video;
    }

    public static class MessageLocation {
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

    public List<String> getImages() {
        return images;
    }

    public List<Video> getVideos() {
        return videos;
    }

}