package com.shoutit.app.android.api.model;

import android.support.annotation.StringRes;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.shoutit.app.android.R;

import java.util.List;

import javax.annotation.Nonnull;

public class MessageAttachment {

    public static final String ATTACHMENT_TYPE_SHOUT = "shout";
    public static final String ATTACHMENT_TYPE_LOCATION = "location";
    public static final String ATTACHMENT_TYPE_MEDIA = "media";
    public static final String ATTACHMENT_TYPE_PROFILE = "profile";

    private final String type;
    private final MessageLocation location;
    private final AttachtmentShout shout;
    private final List<String> images;
    private final List<Video> videos;
    private final MessageProfile profile;

    public MessageAttachment(String type, MessageLocation location, AttachtmentShout shout,
                             List<String> image, List<Video> video, MessageProfile profile) {
        this.type = type;
        this.location = location;
        this.shout = shout;
        this.images = image;
        this.videos = video;
        this.profile = profile;
    }

    public static class MessageProfile {
        @Nonnull
        private final String id;

        public MessageProfile(@Nonnull String id) {
            this.id = id;
        }
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

    public AttachtmentShout getShout() {
        return shout;
    }

    public MessageProfile getProfile() {
        return profile;
    }

    public List<String> getImages() {
        return images;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public static class AttachtmentShout {
        public static final String TYPE_OFFER = "offer";
        public static final String TYPE_REQUEST = "request";
        public static final String TYPE_ALL = "all";

        @Nonnull
        private final String id;
        private final String apiUrl;
        private final String webUrl;
        private final String type;
        private final UserLocation location;
        private final String title;
        private final String text;
        private final Long price;
        private final float number;
        private final String currency;
        private final String thumbnail;
        private final String videoUrl;
        private final User profile;
        private final User user; // api is funny so duplicate field
        private final Category category;
        private final long datePublished;
        private final int availableCount;

        public AttachtmentShout(@Nonnull String id, String apiUrl, String webUrl, String type,
                                UserLocation location, String title, String text, Long price, float number,
                                String currency, String thumbnail, String videoUrl, User profile,
                                Category category, long datePublished, int availableCount) {
            this.user = null;
            this.id = id;
            this.apiUrl = apiUrl;
            this.webUrl = webUrl;
            this.type = type;
            this.location = location;
            this.title = title;
            this.text = text;
            this.price = price;
            this.number = number;
            this.currency = currency;
            this.thumbnail = thumbnail;
            this.videoUrl = videoUrl;
            this.profile = profile;
            this.category = category;
            this.datePublished = datePublished;
            this.availableCount = availableCount;
        }

        @Nonnull
        public String getId() {
            return id;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public String getWebUrl() {
            return webUrl;
        }

        public String getType() {
            return type;
        }

        @StringRes
        public int getTypeResId() {
            return TYPE_OFFER.equalsIgnoreCase(type) ?
                    R.string.shout_type_offer :
                    R.string.shout_type_request;
        }

        public UserLocation getLocation() {
            return location;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }

        public float getNumber() {
            return number;
        }

        public String getCurrency() {
            return currency;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public String getThumbnailOrNull() {
            return Strings.emptyToNull(thumbnail);
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public User getUser() {
            return user == null ? profile : user;
        }

        public Category getCategory() {
            return category;
        }

        public Long getPrice() {
            return price;
        }

        public long getDatePublishedInMillis() {
            return datePublished * 1000;
        }

        public int getAvailableCount() {
            return availableCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttachtmentShout)) return false;
            final AttachtmentShout shout = (AttachtmentShout) o;
            return Float.compare(shout.number, number) == 0 &&
                    Objects.equal(id, shout.id) &&
                    Objects.equal(apiUrl, shout.apiUrl) &&
                    Objects.equal(webUrl, shout.webUrl) &&
                    Objects.equal(type, shout.type) &&
                    Objects.equal(location, shout.location) &&
                    Objects.equal(title, shout.title) &&
                    Objects.equal(text, shout.text) &&
                    Objects.equal(currency, shout.currency) &&
                    Objects.equal(thumbnail, shout.thumbnail) &&
                    Objects.equal(videoUrl, shout.videoUrl) &&
                    Objects.equal(user, shout.user) &&
                    Objects.equal(category, shout.category) &&
                    Objects.equal(datePublished, shout.datePublished) &&
                    Objects.equal(price, shout.price) &&
                    Objects.equal(availableCount, shout.availableCount);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, apiUrl, webUrl, type, location, title, text, price,
                    number, currency, thumbnail, videoUrl, user, category, datePublished, availableCount);
        }
    }


}