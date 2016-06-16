package com.shoutit.app.android.api.model;

public class EditShoutPriceRequest {

    private final long price;
    private final String currency;
    private final boolean publishToFacebook;

    public EditShoutPriceRequest(long price, String currency, boolean publishToFacebook) {
        this.price = price;
        this.currency = currency;
        this.publishToFacebook = publishToFacebook;
    }
}
