package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class CreateRequestShoutRequest {

    private final String type = "request";
    private final String title;
    private final UserLocationSimple location;
    private final long price;
    private final String currency;

    public CreateRequestShoutRequest(@NonNull String title, @NonNull UserLocationSimple location, long price, String currency) {
        this.title = title;
        this.location = location;
        this.price = price;
        this.currency = currency;
    }
}
