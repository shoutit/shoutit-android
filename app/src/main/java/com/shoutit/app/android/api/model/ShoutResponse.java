package com.shoutit.app.android.api.model;

import java.util.List;

public class ShoutResponse {

    private final Long price;
    private final String title;
    private final String text;
    private final String type;
    private final UserLocation location;
    private final String currency;
    private final Category category;
    private final List<String> images;
    private final List<Video> videos;
    private final List<CategoryFilter> filters;
    private final User profile;
    private final long datePublished;
    private final String mobileHint;
    private final String mobile;

    public ShoutResponse(Long price, String title, String text, String type, UserLocation location,
                         String currency, Category category, List<String> images, List<Video> videos,
                         List<CategoryFilter> filters, User profile, long datePublished, String mobileHint, String mobile) {
        this.price = price;
        this.title = title;
        this.text = text;
        this.type = type;
        this.location = location;
        this.currency = currency;
        this.category = category;
        this.images = images;
        this.videos = videos;
        this.filters = filters;
        this.profile = profile;
        this.datePublished = datePublished;
        this.mobileHint = mobileHint;
        this.mobile = mobile;
    }

    public String getMobileHint() {
        return mobileHint;
    }

    public String getMobile() {
        return mobile;
    }

    public Long getPrice() {
        return price;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public UserLocation getLocation() {
        return location;
    }

    public String getCurrency() {
        return currency;
    }

    public Category getCategory() {
        return category;
    }

    public String getText() {
        return text;
    }

    public List<String> getImages() {
        return images;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public List<CategoryFilter> getFilters() {
        return filters;
    }

    public User getProfile() {
        return profile;
    }

    public long getDatePublished() {
        return datePublished;
    }
}