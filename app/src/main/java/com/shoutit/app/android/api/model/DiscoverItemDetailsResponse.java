package com.shoutit.app.android.api.model;

import java.util.List;

public class DiscoverItemDetailsResponse {
    private final String id;
    private final boolean showChildren;
    private final boolean show_shouts;
    private final List<DiscoverChild> children;

    public DiscoverItemDetailsResponse(String id, boolean showChildren, boolean show_shouts, List<DiscoverChild> children) {
        this.id = id;
        this.showChildren = showChildren;
        this.show_shouts = show_shouts;
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public boolean isShowChildren() {
        return showChildren;
    }

    public boolean isShow_shouts() {
        return show_shouts;
    }

    public List<DiscoverChild> getChildren() {
        return children;
    }
}
