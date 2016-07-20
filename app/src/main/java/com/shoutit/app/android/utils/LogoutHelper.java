package com.shoutit.app.android.utils;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.utils.pusher.PusherHelperHolder;
import com.shoutit.app.android.facebook.FacebookHelper;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

public class LogoutHelper {

    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final RecentSearchesTable recentSearchesTable;
    private final PusherHelper mPusherHelper;
    private final ProfilesDao mProfilesDao;
    private final ShoutsDao shoutsDao;
    private final PusherHelper mUserPusherHelper;

    @Inject
    Twilio twilio;

    @Inject
    BookmarksDao bookmarksDao;

    @Inject
    public LogoutHelper(@Nonnull UserPreferences userPreferences,
                        @Nonnull RecentSearchesTable recentSearchesTable,
                        PusherHelperHolder pusherHelper,
                        @Named("user") PusherHelperHolder userPusherHelper,
                        ProfilesDao profilesDao,
                        ShoutsDao shoutsDao) {
        this.userPreferences = userPreferences;
        this.recentSearchesTable = recentSearchesTable;
        mPusherHelper = pusherHelper.getPusherHelper();
        mUserPusherHelper = userPusherHelper.getPusherHelper();
        mProfilesDao = profilesDao;
        this.shoutsDao = shoutsDao;
    }

    public void logout() {
        twilio.unregisterTwillio();

        mProfilesDao.registerToGcmAction(null);

        mPusherHelper.unsubscribeProfileChannel();
        mPusherHelper.disconnect();

        if (mUserPusherHelper != null) {
            mUserPusherHelper.unsubscribeProfileChannel();
            mUserPusherHelper.disconnect();
        }

        userPreferences.logout();
        recentSearchesTable.clearRecentSearches();
        FacebookHelper.logOutFromFacebook();

        shoutsDao.invalidate();
        bookmarksDao.invalidate();
    }
}