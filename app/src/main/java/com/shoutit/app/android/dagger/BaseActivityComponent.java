package com.shoutit.app.android.dagger;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.location.LocationManager;
import com.squareup.picasso.Picasso;

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

    LayoutInflater getLayoutInflater();

    ApiService apiService();

    Gson gson();

    ShoutsDao shoutsDao();

    DiscoversDao discoversDao();

    GoogleApiClient googleApiClient();

    UserPreferences userPreferences();

    LocationManager locationManager();

    DiscoverShoutsDao discoverShoutsDao();
}