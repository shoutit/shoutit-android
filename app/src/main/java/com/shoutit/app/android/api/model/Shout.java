package com.shoutit.app.android.api.model;

import android.support.annotation.StringRes;

import com.google.common.base.Objects;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.PriceUtils;

import java.util.List;

import javax.annotation.Nonnull;

public class Shout {
    private static final String TYPE_OFFER = "offer";
    private static final String TYPE_REQUEST = "request";

    @Nonnull
    private final String id;
    private final String apiUrl;
    private final String webUrl;
    private final String type;
    private final UserLocation location;
    private final String title;
    private final String text;
    private final long price;
    private final float number;
    private final String currency;
    private final String thumbnail;
    private final String videoUrl;
    private final User profile;
    private final Category category;
    private final long datePublished;
    private final List<String> images;
    private final List<String> videos;
    private final List<Filter> filters;
    private final int availableCount;

    public Shout(@Nonnull String id, String apiUrl, String webUrl, String type,
                 UserLocation location, String title, String text, long price, float number,
                 String currency, String thumbnail, String videoUrl, User profile,
                 Category category, List<Filter> filters, long datePublished, List<String> images, List<String> videos, int availableCount) {
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

    public long getPrice() {
        return price;
    }

    public long getDatePublishedInMillis() {
        return datePublished * 1000;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getVideos() {
        return videos;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shout)) return false;
        final Shout shout = (Shout) o;
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
                Objects.equal(profile, shout.profile) &&
                Objects.equal(category, shout.category) &&
                Objects.equal(datePublished, shout.datePublished) &&
                Objects.equal(price, shout.price) &&
                Objects.equal(availableCount, shout.availableCount) &&
                Objects.equal(videos, shout.videos) &&
                Objects.equal(images, shout.images);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, apiUrl, webUrl, type, location, title, text, price,
                number, currency, thumbnail, videoUrl, profile, category, datePublished, images, availableCount, videos);
    }
}
