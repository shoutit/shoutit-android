package com.shoutit.app.android.utils;

import com.pusher.client.Pusher;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.db.RecentSearchesTable;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LogoutHelper {

    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final RecentSearchesTable recentSearchesTable;
    private final PusherHelper mPusherHelper;

    @Inject
    public LogoutHelper(@Nonnull UserPreferences userPreferences,
                        @Nonnull RecentSearchesTable recentSearchesTable,
                        PusherHelper pusherHelper) {
        this.userPreferences = userPreferences;
        this.recentSearchesTable = recentSearchesTable;
        mPusherHelper = pusherHelper;
    }

    public void logout() {
        final Pusher pusher = mPusherHelper.getPusher();
        final User user = userPreferences.getUser();
        assert user != null;
        pusher.unsubscribe(String.format("presence-v3-p-%1$s", user.getId()));
        pusher.disconnect();

        userPreferences.logout();
        recentSearchesTable.clearRecentSearches();
    }
}
