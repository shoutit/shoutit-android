package com.shoutit.app.android.api.model;

import java.util.List;

import javax.annotation.Nonnull;

public class RelatedTagsResponse {
    @Nonnull
    final List<TagDetail> results;

    public RelatedTagsResponse(@Nonnull List<TagDetail> results) {
        this.results = results;
    }

    @Nonnull
    public List<TagDetail> getResults() {
        return results;
    }
}
