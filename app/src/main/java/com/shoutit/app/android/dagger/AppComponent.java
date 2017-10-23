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
import com.shoutit.app.android.AppPreferences;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.db.DbHelper;
import com.shoutit.app.android.facebook.FacebookHelper;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.pusher.PusherHelperHolder;
import com.shoutit.app.android.view.chats.LocalMessageBus;
import com.shoutit.app.android.view.conversations.RefreshConversationBus;
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
                DaoModule.class
        }
)
public interface AppComponent extends DaoComponent {

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

    ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter();

    GoogleApiClient googleApiClient();

    UserPreferences userPreferences();

    AppPreferences appPreferences();

    LocationManager locationManager();

    TransferUtility transferUtility();

    ContentResolver contentResolver();

    DbHelper dbHelper();

    PusherHelperHolder pusher();

    @Named("user")
    PusherHelperHolder userPusher();

    MixPanel mixPanel();

    SharedPreferences sharedPreferences();

    FacebookHelper facebookHelper();

    LocalMessageBus localMessageBus();

    RefreshConversationBus refreshConversationBus();
}