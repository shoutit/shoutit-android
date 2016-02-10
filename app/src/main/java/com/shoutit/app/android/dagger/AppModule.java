package com.shoutit.app.android.dagger;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.appunite.rx.dagger.NetworkScheduler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.AuthInterceptor;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.view.signin.CoarseLocationObservableProvider;
import com.squareup.picasso.Picasso;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import rx.Scheduler;

@Module
public final class AppModule {

    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    @ForApplication
    public Context applicationContext() {
        return app.getApplicationContext();
    }

    @Provides
    @Singleton
    Picasso providePicasso(@ForApplication Context context, OkHttpClient okHttpClient) {
        return new Picasso.Builder(context)
                .indicatorsEnabled(BuildConfig.DEBUG)
                .loggingEnabled(BuildConfig.DEBUG)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        if (BuildConfig.DEBUG) {
                            Log.e("picasso", "error", exception);
                        }
                    }
                })
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(@ForApplication final Context context,
                                            AuthInterceptor authInterceptor) {
        final Optional<Cache> cache = getCacheOrNull(context);
        final OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        if (cache.isPresent()) {
            okHttpClient.cache(cache.get());
        }

        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        okHttpClient.interceptors().add(loggingInterceptor);
        okHttpClient.interceptors().add(authInterceptor);
        loggingInterceptor.setLevel(BuildConfig.DEBUG ?
                HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        return okHttpClient.build();
    }

    @Provides
    @Singleton
    ApiService provideApiService(Gson gson, OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build()
                .create(ApiService.class);
    }

    private Optional<Cache> getCacheOrNull(@ForApplication Context context) {
        final File httpCacheDir = new File(context.getCacheDir(), "cache");
        final long httpCacheSize = 100 * 1024 * 1024; // 100 MiB
        return Optional.of(new Cache(httpCacheDir, httpCacheSize));
    }

    @Provides
    public CoarseLocationObservableProvider provideCoarseLocationObservableProvider() {
        return CoarseLocationObservableProvider.DEFAULT;
    }

    @Singleton
    @Provides
    AuthInterceptor prvideAuthInterceptor(UserPreferences userPreferences) {
        return new AuthInterceptor(userPreferences);
    }

    @Provides
    public ShoutsDao provideShoutsDao(ApiService apiService,
                                      @NetworkScheduler Scheduler networkScheduler,
                                      UserPreferences userPreferences) {
        return new ShoutsDao(apiService, networkScheduler, userPreferences);
    }

    @Provides
    public DiscoversDao provideDiscoversDao(ApiService apiService,
                                            @NetworkScheduler Scheduler networkScheduler,
                                            UserPreferences userPreferences) {
        return new DiscoversDao(apiService, userPreferences, networkScheduler);
    }

    @Provides
    GoogleApiClient providesGoogleApiClient(@ForApplication Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    @Provides
    public LocationManager provideLocationManager(@ForApplication Context context,
                                                  ApiService apiService,
                                                  UserPreferences userPreferences,
                                                  GoogleApiClient googleApiClient,
                                                  @NetworkScheduler Scheduler networkScheduler) {
        return new LocationManager(context, userPreferences, googleApiClient, apiService, networkScheduler);
    }

}