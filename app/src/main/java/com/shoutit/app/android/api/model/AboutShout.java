package com.shoutit.app.android.api.model;

public class AboutShout {

    private final String title;

    private final Long price;

    private final String currency;

    private final String thumbnail;

    private final String type;

    private final User profile;

    private final long datePublished;

    public AboutShout(String title, Long price, String currency, String thumbnail, String type, User profile, long datePublished) {
        this.title = title;
        this.price = price;
        this.currency = currency;
        this.thumbnail = thumbnail;
        this.type = type;
        this.profile = profile;
        this.datePublished = datePublished;
    }

    public String getTitle() {
        return title;
    }

    public Long getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getType() {
        return type;
    }

    public User getProfile() {
        return profile;
    }

    public long getDatePublished() {
        return datePublished;
    }
}