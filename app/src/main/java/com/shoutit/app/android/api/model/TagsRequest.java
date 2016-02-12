package com.shoutit.app.android.api.model;

import java.util.List;

public class TagsRequest {

    public TagsRequest(List<TagToListen> tags) {
        this.tags = tags;
    }

    public static class TagToListen {
        private final String name;

        public TagToListen(String name) {
            this.name = name;
        }
    }

    private final List<TagToListen> tags;

}
