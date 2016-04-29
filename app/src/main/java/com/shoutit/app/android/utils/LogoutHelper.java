package com.shoutit.app.android.utils;

import com.pusher.client.Pusher;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.twilio.Twilio;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LogoutHelper {

    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final RecentSearchesTable recentSearchesTable;
    private final PusherHelper mPusherHelper;
    private final ProfilesDao mProfilesDao;

    @Inject
    Twilio twilio;

    @Inject
    public LogoutHelper(@Nonnull UserPreferences userPreferences,
                        @Nonnull RecentSearchesTable recentSearchesTable,
                        PusherHelper pusherHelper,
                        ProfilesDao profilesDao) {
        this.userPreferences = userPreferences;
        this.recentSearchesTable = recentSearchesTable;
        mPusherHelper = pusherHelper;
        mProfilesDao = profilesDao;
    }

    public void logout() {
        twilio.unregisterTwillio();

        final Pusher pusher = mPusherHelper.getPusher();
        final User user = userPreferences.getUser();
        assert user != null;
        pusher.unsubscribe(PusherHelper.getProfileChannelName(user.getId()));
        pusher.disconnect();

        userPreferences.logout();
        recentSearchesTable.clearRecentSearches();
        mProfilesDao.registerToGcmAction(null);
    }
}
