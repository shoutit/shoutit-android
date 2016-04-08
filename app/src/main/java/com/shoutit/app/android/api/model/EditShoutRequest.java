package com.shoutit.app.android.api.model;

import java.util.List;

public class EditShoutRequest {

    private final String title;
    private final String text;
    private final UserLocationSimple location;
    private final String category;
    private final List<FilterValue> filters;
    private final List<String> images;
    private final List<Video> videos;
    private final String mobile;

    public EditShoutRequest(String title, String text, UserLocationSimple location, String category,
                            List<FilterValue> filters, List<String> images, List<Video> videos, String mobile) {
        this.title = title;
        this.text = text;
        this.location = location;
        this.category = category;
        this.filters = filters;
        this.images = images;
        this.videos = videos;
        this.mobile = mobile;
    }

    public static class FilterValue {

        private final String slug;
        private final Value value;

        public FilterValue(String slug, Value value) {
            this.slug = slug;
            this.value = value;
        }

        public static class Value {
            private final String slug;

            public Value(String slug) {
                this.slug = slug;
            }
        }
    }

}
