package com.shoutit.app.android.api.model;

public class EditShoutPriceRequest {

    private final long price;
    private final String currency;

    public EditShoutPriceRequest(long price, String currency) {
        this.price = price;
        this.currency = currency;
    }
}
