package com.shoutit.app.android.api.model;

import java.util.List;

public class DiscoverItemDetailsResponse {
    private final String id;
    private final boolean showChildren;
    private final boolean showShouts;
    private final List<DiscoverChild> children;
    private final String title;
    private final String image;
    private final String cover;

    public DiscoverItemDetailsResponse(String id, boolean showChildren,
                                       boolean showShouts, List<DiscoverChild> children,
                                       String title, String image, String cover) {
        this.id = id;
        this.showChildren = showChildren;
        this.showShouts = showShouts;
        this.children = children;
        this.title = title;
        this.image = image;
        this.cover = cover;
    }

    public String getId() {
        return id;
    }

    public boolean isShowChildren() {
        return showChildren;
    }

    public boolean isShowShouts() {
        return showShouts;
    }

    public List<DiscoverChild> getChildren() {
        return children;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getCover() {
        return cover;
    }
}
