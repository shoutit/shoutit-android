package com.shoutit.app.android.dagger;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.dao.PagesDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.PublicPagesDaos;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.dao.SuggestionsDao;
import com.shoutit.app.android.db.DbHelper;
import com.shoutit.app.android.dao.TagsDao;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.squareup.picasso.Picasso;


import javax.inject.Named;

import dagger.Component;
import rx.Scheduler;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
        }
)
public interface BaseActivityComponent {

    @ForActivity
    Resources getResources();

    @NetworkScheduler
    Scheduler getNetworkScheduler();

    @UiScheduler
    Scheduler getUiScheduler();

    @ForActivity
    Context getActivityContext();

    @ForApplication
    Context getAppContext();

    Picasso getPicasso();

    @Named("NoAmazonTransformer")
    Picasso getNoAmazonTransformPicasso();

    LayoutInflater getLayoutInflater();

    ApiService apiService();

    Gson gson();

    ShoutsDao shoutsDao();

    TagsDao tagsDao();

    DiscoversDao discoversDao();

    ProfilesDao profilesDao();

    CategoriesDao caregoriesDao();

    SuggestionsDao suggestionsDao();

    ListeningsDao listeningsDao();

    PublicPagesDaos publicPagesDaos();

    GoogleApiClient googleApiClient();

    UserPreferences userPreferences();

    LocationManager locationManager();

    DiscoverShoutsDao discoverShoutsDao();

    TransferUtility transferUtility();

    ContentResolver contentResolver();

    FragmentManager fragmentManager();

    DbHelper dbHelper();

    SortTypesDao sortTypesDao();

    RecentSearchesTable recentSearchesTable();

    ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter();

    PusherHelper pusherHelper();

    MixPanel mixPanel();

    PagesDao pagesDao();

    BookmarksDao bookmarkDao();
}