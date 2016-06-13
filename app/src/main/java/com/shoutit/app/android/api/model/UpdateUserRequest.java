package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

public class UpdateUserRequest {

    @Nullable
    private final String username;
    @Nullable
    private final String firstName;
    @Nullable
    private final String lastName;
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
    private final String mobile;
    @Nullable
    private final String email;
    @Nullable
    private final String gender;
    @Nullable
    private final String birthday;

    public static UpdateUserRequest updateWithEmail(String email) {
        return new UpdateUserRequest(null, null, null, email, null, null, null, null, null, null, null, null);
    }

    public static UpdateUserRequest updateWithCoverUrl(String coverUrl) {
        return new UpdateUserRequest(null, null, null, null, null, null, null, coverUrl, null, null, null, null);
    }

    public static UpdateUserRequest updateWithAvatarUrl(String avatarUrl) {
        return new UpdateUserRequest(null, null, null, null, null, null, null, null, avatarUrl, null, null, null);
    }

    public static UpdateUserRequest updateProfile(@Nullable String username,
                                                  @Nullable String firstName,
                                                  @Nullable String lastName,
                                                  @Nullable String bio,
                                                  @Nullable String website,
                                                  @Nullable String mobile,
                                                  @Nullable String gender,
                                                  @Nullable String birthdayTimestamp,
                                                  @Nullable UserLocation location) {
        return new UpdateUserRequest(firstName, lastName, username, null, bio, website,
                location, null, null, mobile, gender, birthdayTimestamp);
    }

    public UpdateUserRequest(@Nullable String firstName, @Nullable String lastName,
                             @Nullable String username, @Nullable String email,
                             @Nullable String bio, @Nullable String website,
                             @Nullable UserLocation location, @Nullable String cover,
                             @Nullable String image, @Nullable String mobile,
                             @Nullable String gender, @Nullable String birthdayTimestamp) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.website = website;
        this.location = location;
        this.cover = cover;
        this.image = image;
        this.mobile = mobile;
        this.gender = gender;
        this.birthday = birthdayTimestamp;
    }
}
