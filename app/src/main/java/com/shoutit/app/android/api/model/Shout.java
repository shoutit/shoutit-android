package com.shoutit.app.android.api.model;

import java.util.List;

import javax.annotation.Nonnull;

public class Shout {
    @Nonnull
    private final String id;
    private final String apiUrl;
    private final String webUrl;
    private final String type;
    private final Location location;
    private final String title;
    private final String text;
    private final float number;
    private final String currency;
    private final String thumbnail;
    private final String videoUrl;
    private final User user;
    private final Category category;
    private final List<Tag> tags;
    private final String tags2;

    public Shout(@Nonnull String id, String apiUrl, String webUrl, String type,
                 Location location, String title, String text, float number,
                 String currency, String thumbnail, String videoUrl, User user,
                 Category category, List<Tag> tags, String tags2) {
        this.id = id;
        this.apiUrl = apiUrl;
        this.webUrl = webUrl;
        this.type = type;
        this.location = location;
        this.title = title;
        this.text = text;
        this.number = number;
        this.currency = currency;
        this.thumbnail = thumbnail;
        this.videoUrl = videoUrl;
        this.user = user;
        this.category = category;
        this.tags = tags;
        this.tags2 = tags2;
    }
}
