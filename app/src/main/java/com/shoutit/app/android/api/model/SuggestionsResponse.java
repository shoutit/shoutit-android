package com.shoutit.app.android.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class SuggestionsResponse {

    @Nonnull
    private final List<BaseProfile> users;
    @Nonnull
    private final List<BaseProfile> pages;

    public SuggestionsResponse(@Nonnull List<BaseProfile> users, @Nonnull List<BaseProfile> pages) {
        this.users = users;
        this.pages = pages;
    }

    @Nonnull
    public List<BaseProfile> getUsers() {
        return users;
    }

    @Nonnull
    public List<BaseProfile> getPages() {
        return pages;
    }

    @Nonnull
    public SuggestionsResponse withUpdatedUser(BaseProfile baseProfile) {
        final List<BaseProfile> newUsers = new ArrayList<>(users);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(baseProfile.getId())) {
                newUsers.set(i, baseProfile);
                break;
            }
        }

        return new SuggestionsResponse(newUsers, pages);
    }

    @Nonnull
    public SuggestionsResponse withUpdatedPage(BaseProfile baseProfile) {
        final List<BaseProfile> newPages = new ArrayList<>(pages);
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getId().equals(baseProfile.getId())) {
                newPages.set(i, baseProfile);
                break;
            }
        }

        return new SuggestionsResponse(users, newPages);
    }
}
