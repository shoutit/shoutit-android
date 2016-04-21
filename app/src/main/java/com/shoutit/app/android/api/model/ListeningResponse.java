package com.shoutit.app.android.api.model;

import java.util.List;

import javax.annotation.Nullable;

public class ListeningResponse extends PaginatedResponse {

    @Nullable
    private final List<User> users;
    @Nullable
    private final List<Page> pages;
    @Nullable
    private final List<TagDetail> tags;

    public ListeningResponse(int count, @Nullable String next, @Nullable String previous,
                             @Nullable List<User> users, @Nullable List<Page> pages,
                             @Nullable List<TagDetail> tags) {
        super(count, next, previous);
        this.users = users;
        this.pages = pages;
        this.tags = tags;
    }

    @Nullable
    public List<User> getUsers() {
        return users;
    }

    @Nullable
    public List<Page> getPages() {
        return pages;
    }

    @Nullable
    public List<TagDetail> getTags() {
        return tags;
    }
}
