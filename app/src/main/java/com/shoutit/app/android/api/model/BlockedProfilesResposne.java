package com.shoutit.app.android.api.model;

import java.util.List;

public class BlockedProfilesResposne {

    private final String next;
    private final String previous;
    private final List<ConversationProfile> results;

    public BlockedProfilesResposne(String next, String previous, List<ConversationProfile> profiles) {
        this.next = next;
        this.previous = previous;
        this.results = profiles;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<ConversationProfile> getProfiles() {
        return results;
    }
}