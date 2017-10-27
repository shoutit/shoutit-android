package com.shoutit.app.android.api.model;

import java.util.List;

public class EditShoutRequest {

    private final String title;
    private final String text;
    private final UserLocationSimple location;
    private final String category;
    private final List<Filter> filters;
    private final List<String> images;
    private final List<Video> videos;
    private final String mobile;

    public EditShoutRequest(String title, String text, UserLocationSimple location, String category,
                            List<Filter> filters, List<String> images, List<Video> videos, String mobile) {
        this.title = title;
        this.text = text;
        this.location = location;
        this.category = category;
        this.filters = filters;
        this.images = images;
        this.videos = videos;
        this.mobile = mobile;
    }
}
