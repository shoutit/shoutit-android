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

    public class DiscoverChild {
        private final String id;
        private final String apiUrl;
        private final String title;
        private final String subtitle;
        private final String image;
        private final String icon;

        public DiscoverChild(String id, String apiUrl, String title, String subtitle, String image, String icon) {
            this.id = id;
            this.apiUrl = apiUrl;
            this.title = title;
            this.subtitle = subtitle;
            this.image = image;
            this.icon = icon;
        }
    }

}
