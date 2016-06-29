package com.shoutit.app.android.utils;


import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class PreferencesHelper {

    @Nullable
    private final User currentUser;

    @Inject
    public PreferencesHelper(@Nonnull UserPreferences preferences) {
        currentUser = preferences.getUser();
    }

    public boolean isMyProfile(@Nonnull String userNameToCompare) {
        return currentUser != null &&
                (userNameToCompare.equals(currentUser.getUsername()) || userNameToCompare.equals(User.ME));
    }
}
