package com.shoutit.app.android.api.model;

public class Video {
    private final String url;
    private final String thumbnailUrl;
    private final String provider;
    private final String idOnProvider;
    private final int duration;

    public Video(String url, String thumbnailUrl, String provider, String idOnProvider, int duration) {
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.provider = provider;
        this.idOnProvider = idOnProvider;
        this.duration = duration;
    }
}
