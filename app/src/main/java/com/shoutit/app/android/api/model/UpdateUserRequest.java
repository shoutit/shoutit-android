package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

public class UpdateUserRequest {

    @Nullable
    private final String username;
    @Nullable
    private final String name;
    @Nullable
    private final String bio;
    @Nullable
    private final String website;
    @Nullable
    private final UserLocation location;
    @Nullable
    private final String cover;
    @Nullable
    private final String image;

    @Nullable
    private final String email;

    public static UpdateUserRequest updateWithEmail(String email) {
        return new UpdateUserRequest(email, null, null, null, null, null, null, null);
    }

    public static UpdateUserRequest updateWithCoverUrl(String coverUrl) {
        return new UpdateUserRequest(null, null, null, null, null, null, coverUrl, null);
    }

    public static UpdateUserRequest updateWithAvatarUrl(String avatarUrl) {
        return new UpdateUserRequest(null, null, null, null, null, null, null, avatarUrl);
    }

    public static UpdateUserRequest updateProfile(@Nullable String username,
                                                  @Nullable String name,
                                                  @Nullable String bio,
                                                  @Nullable String website,
                                                  @Nullable UserLocation location) {
        return new UpdateUserRequest(null, username, name, bio, website, location, null, null);
    }

    public UpdateUserRequest(@Nullable String email, @Nullable String username,
                             @Nullable String name,
                             @Nullable String bio, @Nullable String website,
                             @Nullable UserLocation location, @Nullable String cover,
                             @Nullable String image) {
        this.email = email;
        this.username = username;
        this.name = name;
        this.bio = bio;
        this.website = website;
        this.location = location;
        this.cover = cover;
        this.image = image;
    }
}
