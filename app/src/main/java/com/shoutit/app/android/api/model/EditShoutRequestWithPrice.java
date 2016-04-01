package com.shoutit.app.android.api.model;

import java.util.List;

public class EditShoutRequestWithPrice extends EditShoutRequest {

    private final long price;
    private final String currency;

    public EditShoutRequestWithPrice(String title, String description, UserLocationSimple location,
                                     long price, String currency, String category,
                                     List<FilterValue> filters, List<String> images, List<Video> videos) {
        super(title, description, location, category, filters, images, videos);
        this.price = price;
        this.currency = currency;
    }
}
