package com.shoutit.app.android.api.model;

import java.util.List;

public class TagsRequest {

    public TagsRequest(List<TagToListen> tags) {
        this.tags = tags;
    }

    public static class TagToListen {
        private final String slug;

        public TagToListen(String slug) {
            this.slug = slug;
        }
    }

    private final List<TagToListen> tags;

}
