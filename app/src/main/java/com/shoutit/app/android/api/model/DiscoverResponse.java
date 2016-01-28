package com.shoutit.app.android.api.model;

import java.util.List;

import javax.annotation.Nonnull;

public class DiscoverResponse extends PaginatedResponse {

    @Nonnull
    private final List<Discover> results;

    public DiscoverResponse(@Nonnull List<Discover> results) {
        this.results = results;
    }

    private class Discover {
        @Nonnull
        private final String id;
        private final String apiUrl;
        private final String title;
        private final String subtitle;
        private final int position;
        private final String image;
        private final String icon;

        public Discover(@Nonnull String id, String apiUrl, String title, String subtitle,
                        int position, String image, String icon) {
            this.id = id;
            this.apiUrl = apiUrl;
            this.title = title;
            this.subtitle = subtitle;
            this.position = position;
            this.image = image;
            this.icon = icon;
        }

        @Nonnull
        public String getId() {
            return id;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public int getPosition() {
            return position;
        }

        public String getImage() {
            return image;
        }

        public String getIcon() {
            return icon;
        }
    }

    @Nonnull
    public List<Discover> getResults() {
        return results;
    }
}
