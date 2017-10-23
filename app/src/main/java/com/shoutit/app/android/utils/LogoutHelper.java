package com.shoutit.app.android.utils;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.view.loginintro.FacebookHelper;

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
        mProfilesDao.registerToGcmAction(null);

        final User user = userPreferences.getUser();
        assert user != null;
        mPusherHelper.unsubscribeProfileChannel(user.getId());
        mPusherHelper.disconnect();

        userPreferences.logout();
        recentSearchesTable.clearRecentSearches();
        FacebookHelper.logOutFromFacebook();
    }
}
