package com.shoutit.app.android.api.model;

import android.net.Uri;
import android.support.annotation.NonNull;

public class Video {

    private final String url;
    private final String thumbnailUrl;
    private final String provider = "shoutit_s3";
    private final String idOnProvider;
    private final int duration;

    public static Video createVideo(String url, String thumbnail) {
        return new Video(url, thumbnail, Uri.parse(url).getLastPathSegment(), 40); // TODO duration
    }

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

    public String getUrl() {
        return url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getProvider() {
        return provider;
    }

    public String getIdOnProvider() {
        return idOnProvider;
    }

    public int getDuration() {
        return duration;
    }
}