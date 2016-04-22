package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

import java.util.List;

public class MessagesResponse {

    @NonNull
    private final String next;
    @NonNull
    private final String previous;
    @NonNull
    private final List<Message> results;

    public MessagesResponse(@NonNull String next, @NonNull String previous, @NonNull List<Message> results) {
        this.next = next;
        this.previous = previous;
        this.results = results;
    }

    @NonNull
    public String getPrevious() {
        return previous;
    }

    @NonNull
    public String getNext() {
        return next;
    }

    @NonNull
    public List<Message> getResults() {
        return results;
    }
}
