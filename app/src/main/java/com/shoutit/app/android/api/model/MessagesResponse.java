package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

import java.util.List;

public class MessagesResponse {

    @NonNull
    private final String next;
    @NonNull
    private final List<Message> results;

    public MessagesResponse(@NonNull String next, @NonNull List<Message> results) {
        this.next = next;
        this.results = results;
    }

    public String getNext() {
        return next;
    }

    @NonNull
    public List<Message> getResults() {
        return results;
    }
}
