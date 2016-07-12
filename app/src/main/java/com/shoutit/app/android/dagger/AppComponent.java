package com.shoutit.app.android.dagger;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.shoutit.app.android.App;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
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
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.dao.SuggestionsDao;
import com.shoutit.app.android.dao.TagsDao;
import com.shoutit.app.android.dao.VideoCallsDao;
import com.shoutit.app.android.db.DbHelper;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.view.chats.LocalMessageBus;
import com.shoutit.app.android.view.conversations.RefreshConversationBus;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.shoutit.app.android.view.videoconversation.CameraTool;
import com.squareup.picasso.Picasso;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import rx.Scheduler;

@Singleton
@Component(
        modules = {
                AppModule.class,
                BaseModule.class,
        }
)
public interface AppComponent {

    void inject(App app);

    @UiScheduler
    Scheduler getUiScheduler();

    @NetworkScheduler
    Scheduler getNetworkScheduler();

    @ForApplication
    Context getContext();

    Picasso getPicasso();

    @Named("NoAmazonTransformer")
    Picasso getNoAmazonTransformPicasso();

    Gson gson();

    ApiService getApiService();

    ShoutsDao shoutsDao();

    TagsDao tagsDao();

    DiscoversDao discoversDao();

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

    ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter();

    NotificationsDao notificationsDao();

    ConversationMediaDaos conversationMediaDaos();

    GoogleApiClient googleApiClient();

    UserPreferences userPreferences();

    LocationManager locationManager();

    TransferUtility transferUtility();

    ContentResolver contentResolver();

    DbHelper dbHelper();

    PusherHelper pusher();

    VideoCallsDao videoCallsDao();

    Twilio twilio();

    MixPanel mixPanel();

    CameraTool cameraTool();

    SharedPreferences sharedPreferences();

    FacebookHelper facebookHelper();

    LocalMessageBus localMessageBus();

    RefreshConversationBus refreshConversationBus();

    PromoteLabelsDao promoteDao();

    BookmarksDao bookmarkDao();

    BusinessVerificationDaos businessVerificationDaos();
}