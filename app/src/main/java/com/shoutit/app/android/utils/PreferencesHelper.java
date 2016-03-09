package com.shoutit.app.android.utils;


import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class PreferencesHelper {

    @Nonnull
    private final UserPreferences preferences;

    @Inject
    public PreferencesHelper(@Nonnull UserPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isMyProfile(@Nonnull String userNameToCompare) {
        final User currentUser = preferences.getUser();
        return currentUser != null &&
                (userNameToCompare.equals(currentUser.getUsername()) || userNameToCompare.equals(User.ME));
    }
}
