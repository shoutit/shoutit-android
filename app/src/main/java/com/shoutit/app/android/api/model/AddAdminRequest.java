package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class AddAdminRequest {

    @Nonnull
    private final Profile profile;

    public AddAdminRequest(@Nonnull String userId) {
        this.profile = new Profile(userId);
    }

    private class Profile {
        @Nonnull
        private final String id;

        private Profile(@Nonnull String id) {
            this.id = id;
        }
    }
}
