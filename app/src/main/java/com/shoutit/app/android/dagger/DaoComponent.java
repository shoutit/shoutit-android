package com.shoutit.app.android.dagger;

import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.dao.BusinessVerificationDaos;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.ConversationMediaDaos;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.dao.NotificationsDao;
import com.shoutit.app.android.dao.PagesDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.PromoteLabelsDao;
import com.shoutit.app.android.dao.PromoteOptionsDao;
import com.shoutit.app.android.dao.PublicPagesDaos;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.dao.SuggestionsDao;
import com.shoutit.app.android.dao.TagListDaos;
import com.shoutit.app.android.dao.TagsDao;
import com.shoutit.app.android.dao.VideoCallsDao;

public interface DaoComponent {

    ShoutsDao shoutsDao();

    TagsDao tagsDao();

    DiscoversDao discoversDao();

    TagListDaos tagListDaos();

    PagesDao pagesDao();

    ProfilesDao profilesDao();

    SuggestionsDao suggestionsDao();

    DiscoverShoutsDao discoverShoutsDao();

    CategoriesDao categoriesDao();

    SortTypesDao sortTypesDao();

    ListenersDaos listenersDaos();

    ListeningsDao listeningsDao();

    PublicPagesDaos publicPagesDaos();

    PromoteOptionsDao promoteOptionsDao();

    NotificationsDao notificationsDao();

    ConversationMediaDaos conversationMediaDaos();

    VideoCallsDao videoCallsDao();

    PromoteLabelsDao promoteDao();

    BookmarksDao bookmarkDao();

    BusinessVerificationDaos businessVerificationDaos();
}
