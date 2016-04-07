package com.shoutit.app.android.api.model;

import java.util.List;

public class ConversationsResponse {

    private final String next;
    private final List<Conversation> results;

    public ConversationsResponse(String next, List<Conversation> results) {
        this.next = next;
        this.results = results;
    }

    public String getNext() {
        return next;
    }

    public List<Conversation> getResults() {
        return results;
    }

}
