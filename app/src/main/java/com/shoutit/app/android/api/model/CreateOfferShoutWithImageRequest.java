package com.shoutit.app.android.api.model;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class CreateOfferShoutWithImageRequest {

    private final String type = "offer";
    private final List<String> images;
    private final List<CreateOfferShoutWithImageRequest.Video> videos;

    public CreateOfferShoutWithImageRequest(@NonNull List<String> images, @NonNull List<Video> videos) {
        this.images = images;
        this.videos = videos;
    }

    public static CreateOfferShoutWithImageRequest withImage(@NonNull String url) {
        return new CreateOfferShoutWithImageRequest(ImmutableList.of(url), ImmutableList.<Video>of());
    }

    public static CreateOfferShoutWithImageRequest withVideo(@NonNull String thumbnail, @NonNull String url) {
        final Video video = new Video(url, thumbnail, Uri.parse(url).getLastPathSegment(), 40); // TODO duration
        return new CreateOfferShoutWithImageRequest(ImmutableList.<String>of(), ImmutableList.of(video));
    }

    private static class Video {

        private final String url;
        private final String thumbnailUrl;
        private final String provider = "shoutit_s3";
        private final String idOnProvider;
        private final int duration;

        public Video(
                @NonNull String url,
                @NonNull String thumbnailUrl,
                @NonNull String idOnProvider,
                int duration) {
            this.url = url;
            this.thumbnailUrl = thumbnailUrl;
            this.idOnProvider = idOnProvider;
            this.duration = duration;
        }
    }
}