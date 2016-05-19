package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

import javax.annotation.Nonnull;


public class ConversationMediaResponse extends PaginatedResponse {

    @Nonnull
    private List<MessageAttachment> results;

    public ConversationMediaResponse(int count, @Nullable String next, @Nullable String previous,
                                     @Nonnull List<MessageAttachment> results) {
        super(count, next, previous);
        this.results = results;
    }

    @Nonnull
    public List<MessageAttachment> getResults() {
        return results;
    }
}
