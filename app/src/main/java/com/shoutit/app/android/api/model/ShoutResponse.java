package com.shoutit.app.android.api.model;

public class ShoutResponse {

    private final double price;
    private final String title;
    private final String type;
    private final UserLocation location;
    private final String currency;

    public ShoutResponse(double price, String title, String type, UserLocation location, String currency) {
        this.price = price;
        this.title = title;
        this.type = type;
        this.location = location;
        this.currency = currency;
    }
}
