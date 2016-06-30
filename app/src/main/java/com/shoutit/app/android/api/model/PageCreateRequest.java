package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class PageCreateRequest {

    @Nonnull
    private final String pageName;
    @Nonnull
    private final PageCategory pageCategory;

    public PageCreateRequest(@Nonnull String categorySlug,
                             @Nonnull String pageName) {
        this.pageName = pageName;
        pageCategory = new PageCategory(categorySlug);
    }

    private static class PageCategory {
        private final String slug;

        public PageCategory(String slug) {
            this.slug = slug;
        }
    }
}
