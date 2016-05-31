package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class CreateOfferShoutWithImageRequest {

    private final String type = "offer";
    private final List<String> images;
    private final List<Video> videos;
    private final boolean publishToFacebook;

    public CreateOfferShoutWithImageRequest(@NonNull List<String> images, @NonNull List<Video> videos, boolean publishToFacebook) {
        this.images = images;
        this.videos = videos;
        this.publishToFacebook = publishToFacebook;
    }

    public static CreateOfferShoutWithImageRequest withImage(@NonNull String url, boolean publishToFacebook) {
        return new CreateOfferShoutWithImageRequest(ImmutableList.of(url), ImmutableList.<Video>of(), publishToFacebook);
    }

    public static CreateOfferShoutWithImageRequest withVideo(@NonNull String thumbnail, @NonNull String url, boolean publishToFacebook) {
        return new CreateOfferShoutWithImageRequest(ImmutableList.<String>of(), ImmutableList.of(Video.createVideo(url, thumbnail, 40)), publishToFacebook);// TODO duration
    }
}