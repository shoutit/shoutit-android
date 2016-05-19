package com.shoutit.app.android.api.model;

import android.support.annotation.StringRes;

import com.google.common.base.Objects;
import com.shoutit.app.android.R;

import java.util.List;

import javax.annotation.Nonnull;

public class Shout {
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
    private final Category category;
    private final long datePublished;
    private final List<String> images;
    private final List<Video> videos;
    private final List<Filter> filters;
    private final int availableCount;
    private final Boolean isMobileSet;
    private final List<ConversationDetails> conversations;
    private final String mobileHint;
    private final String mobile;

    public Shout(@Nonnull String id, String apiUrl, String webUrl, String type,
                 UserLocation location, String title, String text, Long price, float number,
                 String currency, String thumbnail, String videoUrl, User profile,
                 Category category, List<Filter> filters, long datePublished, List<String> images,
                 List<Video> videos, int availableCount, List<ConversationDetails> conversations, boolean isMobileSet,
                 String mobileHint, String mobile) {
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
        this.images = images;
        this.filters = filters;
        this.videos = videos;
        this.availableCount = availableCount;
        this.isMobileSet = isMobileSet;
        this.conversations = conversations;
        this.mobileHint = mobileHint;
        this.mobile = mobile;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public User getProfile() {
        return profile;
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

    public List<String> getImages() {
        return images;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public Boolean isMobileSet() {
        return isMobileSet;
    }

    public List<ConversationDetails> getConversations() {
        return conversations;
    }

    public long getDatePublished() {
        return datePublished;
    }

    public Boolean getMobileSet() {
        return isMobileSet;
    }

    public String getMobile() {
        return mobile;
    }

    public String getMobileHint() {
        return mobileHint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shout)) return false;
        final Shout shout = (Shout) o;
        return Float.compare(shout.number, number) == 0 &&
                datePublished == shout.datePublished &&
                availableCount == shout.availableCount &&
                Objects.equal(id, shout.id) &&
                Objects.equal(apiUrl, shout.apiUrl) &&
                Objects.equal(webUrl, shout.webUrl) &&
                Objects.equal(type, shout.type) &&
                Objects.equal(location, shout.location) &&
                Objects.equal(title, shout.title) &&
                Objects.equal(text, shout.text) &&
                Objects.equal(price, shout.price) &&
                Objects.equal(currency, shout.currency) &&
                Objects.equal(thumbnail, shout.thumbnail) &&
                Objects.equal(videoUrl, shout.videoUrl) &&
                Objects.equal(profile, shout.profile) &&
                Objects.equal(category, shout.category) &&
                Objects.equal(images, shout.images) &&
                Objects.equal(videos, shout.videos) &&
                Objects.equal(filters, shout.filters) &&
                Objects.equal(isMobileSet, shout.isMobileSet) &&
                Objects.equal(mobile, shout.mobile) &&
                Objects.equal(mobileHint, shout.mobileHint) &&
                Objects.equal(conversations, shout.conversations);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, apiUrl, webUrl, type, location, title, text, price, number,
                currency, thumbnail, videoUrl, profile, category, datePublished, images, videos,
                filters, availableCount, isMobileSet, conversations, mobile, mobileHint);
    }
}
