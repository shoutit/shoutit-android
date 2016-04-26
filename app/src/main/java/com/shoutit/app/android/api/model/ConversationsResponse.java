package com.shoutit.app.android.api.model;

import java.util.List;

public class ConversationsResponse {

    private final String next;
    private final String previous;
    private final List<Conversation> results;

    public ConversationsResponse(String next, String previous, List<Conversation> results) {
        this.next = next;
        this.previous = previous;
        this.results = results;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Conversation> getResults() {
        return results;
    }

}
