package com.shoutit.app.android.api.model;

public class ShoutResponse {

    private final long price;
    private final String title;
    private final String description;
    private final String type;
    private final UserLocation location;
    private final String currency;
    private final Category category;

    public ShoutResponse(long price, String title, String description, String type, UserLocation location, String currency, Category category) {
        this.price = price;
        this.title = title;
        this.description = description;
        this.type = type;
        this.location = location;
        this.currency = currency;
        this.category = category;
    }

    public long getPrice() {
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

    public String getDescription() {
        return description;
    }
}
