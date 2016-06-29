package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

public class UserSuggestionResponse extends ProfilesListResponse {

    @Nonnull
    private final List<BaseProfile> users;

    public UserSuggestionResponse(final int count, @Nullable final String next, @Nullable final String previous, @Nonnull final List<BaseProfile> users) {
        super(count, next, previous, users);
        this.users = users;
    }

    @Nonnull
    @Override
    public List<BaseProfile> getResults() {
        return users;
    }
}
