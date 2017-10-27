package com.shoutit.app.android.api.model;

public class CreatePageRequest {

    private final String pageName;
    private final PageCategory pageCategory;

    public CreatePageRequest(String pageName, String pageCategory) {
        this.pageName = pageName;
        this.pageCategory = new PageCategory(pageCategory);
    }

    private static class PageCategory {
        private final String slug;

        public PageCategory(String slug) {
            this.slug = slug;
        }
    }

}
