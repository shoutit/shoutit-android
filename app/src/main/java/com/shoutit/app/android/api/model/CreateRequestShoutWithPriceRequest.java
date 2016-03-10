package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class CreateRequestShoutWithPriceRequest extends CreateRequestShoutRequest {

    private final long price;
    private final String currency;

    public CreateRequestShoutWithPriceRequest(@NonNull String title, @NonNull UserLocationSimple location, long price, @NonNull String currency) {
        super(title, location);
        this.price = price;
        this.currency = currency;
    }
}
