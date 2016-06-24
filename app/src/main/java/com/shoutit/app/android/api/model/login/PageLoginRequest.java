package com.shoutit.app.android.api.model.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("unused")
public class PageLoginRequest extends BaseLoginRequest {

    private static final String PAGE_LOGIN = "shoutit_page";

    private final String email;
    private final String password;
    private final String pageName;
    private final String name;
    private final PageCategory pageCategory;

    public PageLoginRequest(@NonNull String email,
                            @NonNull String password,
                            @Nullable LoginProfile loginProfile,
                            @NonNull String mixpanelDistinctId,
                            String categorySlug,
                            String pageName,
                            String name) {
        super(mixpanelDistinctId, PAGE_LOGIN, loginProfile);
        this.email = email;
        this.password = password;
        this.pageName = pageName;
        this.name = name;
        pageCategory = new PageCategory(categorySlug);
    }

    private static class PageCategory {
        private final String slug;

        public PageCategory(String slug) {
            this.slug = slug;
        }
    }
}