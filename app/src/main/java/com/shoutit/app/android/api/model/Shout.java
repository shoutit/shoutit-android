package com.shoutit.app.android.api.model;

import android.support.annotation.StringRes;

import com.google.common.base.Objects;
import com.shoutit.app.android.R;

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
    private final float price;
    private final float number;
    private final String currency;
    private final String thumbnail;
    private final String videoUrl;
    private final User user;
    private final Category category;
    private final List<Tag> tags;
    private final long datePublished;
    private final List<String> images;

    public Shout(@Nonnull String id, String apiUrl, String webUrl, String type,
                 UserLocation location, String title, String text, float price, float number,
                 String currency, String thumbnail, String videoUrl, User user,
                 Category category, List<Tag> tags, long datePublished, List<String> images) {
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
        this.user = user;
        this.category = category;
        this.tags = tags;
        this.datePublished = datePublished;
        this.images = images;
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

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public float getPrice() {
        return price;
    }

    public long getDatePublishedInMillis() {
        return datePublished * 1000;
    }

    public List<String> getImages() {
        return images;
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
                Objects.equal(user, shout.user) &&
                Objects.equal(category, shout.category) &&
                Objects.equal(tags, shout.tags) &&
                Objects.equal(datePublished, shout.datePublished) &&
                Objects.equal(price, shout.price) &&
                Objects.equal(images, shout.images);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, apiUrl, webUrl, type, location, title, text, price,
                number, currency, thumbnail, videoUrl, user, category, tags, datePublished, images);
    }
}
