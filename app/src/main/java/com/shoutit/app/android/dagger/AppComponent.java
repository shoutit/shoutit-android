package com.shoutit.app.android.dagger;

import android.content.ContentResolver;
import android.content.Context;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.shoutit.app.android.App;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.NotificationsDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.dao.SuggestionsDao;
import com.shoutit.app.android.db.DbHelper;
import com.shoutit.app.android.dao.TagsDao;
import com.shoutit.app.android.location.LocationManager;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;
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

    OkHttpClient getOkHttpClient();

    Gson gson();

    ApiService getApiService();

    ShoutsDao shoutsDao();

    TagsDao tagsDao();

    DiscoversDao discoversDao();

    ProfilesDao profilesDao();

    SuggestionsDao suggestionsDao();

    DiscoverShoutsDao discoverShoutsDao();

    CategoriesDao categoriesDao();

    SortTypesDao sortTypesDao();

    ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter();

    NotificationsDao notificationsDao();

    GoogleApiClient googleApiClient();

    UserPreferences userPreferences();

    LocationManager locationManager();

    TransferUtility transferUtility();

    ContentResolver contentResolver();

    DbHelper dbHelper();
}