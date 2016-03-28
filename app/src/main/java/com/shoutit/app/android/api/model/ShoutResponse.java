package com.shoutit.app.android.api.model;

public class ShoutResponse {

    private final Long price;
    private final String title;
    private final String text;
    private final String type;
    private final UserLocation location;
    private final String currency;
    private final Category category;

    public ShoutResponse(Long price, String title, String text, String type, UserLocation location, String currency, Category category) {
        this.price = price;
        this.title = title;
        this.text = text;
        this.type = type;
        this.location = location;
        this.currency = currency;
        this.category = category;
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
}