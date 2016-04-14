package com.shoutit.app.android.utils;


import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.twilio.Twilio;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LogoutHelper {

    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final RecentSearchesTable recentSearchesTable;

    @Inject
    Twilio twilio;

    @Inject
    public LogoutHelper(@Nonnull UserPreferences userPreferences,
                        @Nonnull RecentSearchesTable recentSearchesTable) {
        this.userPreferences = userPreferences;
        this.recentSearchesTable = recentSearchesTable;
    }

    public void logout() {
        twilio.unregisterTwillio();
        userPreferences.logout();
        recentSearchesTable.clearRecentSearches();
    }
}
