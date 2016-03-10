package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

public class EditShoutRequest {

    private final String title;
    private final UserLocationSimple location;
    private final String category;
    private final List<FilterValue> filters;

    public EditShoutRequest(String title, UserLocationSimple location, String category, List<FilterValue> filters) {
        this.title = title;
        this.location = location;
        this.category = category;
        this.filters = filters;
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
