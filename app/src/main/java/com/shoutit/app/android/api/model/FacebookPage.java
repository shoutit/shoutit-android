package com.shoutit.app.android.api.model;

import java.util.List;

public class FacebookPage {

    private final String facebookId;
    private final String name;
    private final List<String> perms;

    public FacebookPage(String facebookId, String name, List<String> perms) {
        this.facebookId = facebookId;
        this.name = name;
        this.perms = perms;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public String getName() {
        return name;
    }

    public List<String> getPerms() {
        return perms;
    }
}
