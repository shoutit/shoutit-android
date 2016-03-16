package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class CreateOfferShoutWithImageRequest {

    private final String type = "offer";
    private final List<String> images;

    public CreateOfferShoutWithImageRequest(@NonNull List<String> images) {
        this.images = images;
    }

    public static CreateOfferShoutWithImageRequest withImage(@NonNull String url) {
        return new CreateOfferShoutWithImageRequest(ImmutableList.of(url));
    }
}