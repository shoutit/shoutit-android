package com.shoutit.app.android.dagger;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.appunite.rx.android.NetworkObservableProviderImpl;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.observables.NetworkObservableProvider;
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
import com.shoutit.app.android.constants.AmazonConstants;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.ConversationMediaDaos;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.dao.NotificationsDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.dao.SuggestionsDao;
import com.shoutit.app.android.dao.TagsDao;
import com.shoutit.app.android.dao.UsersIdentityDao;
import com.shoutit.app.android.dao.VideoCallsDao;
import com.shoutit.app.android.db.DbHelper;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.utils.AmazonRequestTransfomer;
import com.shoutit.app.android.utils.VersionUtils;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.view.chats.LocalMessageBus;
import com.shoutit.app.android.view.conversations.RefreshConversationBus;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.shoutit.app.android.view.videoconversation.CameraTool;
import com.shoutit.app.android.view.videoconversation.CameraToolImpl;
import com.shoutit.app.android.view.videoconversation.CameraToolImplLollipop;
import com.squareup.picasso.Picasso;

import java.io.File;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Lazy;
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
    Picasso providePicasso(@ForApplication Context context,
                           @Named("picasso") OkHttpClient okHttpClient) {
        return new Picasso.Builder(context)
                .indicatorsEnabled(BuildConfig.DEBUG)
                .loggingEnabled(BuildConfig.DEBUG)
                .requestTransformer(new AmazonRequestTransfomer())
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
    @Named("NoAmazonTransformer")
    Picasso providePicassoWithNoAmazonTransformer(@ForApplication Context context,
                                                  @Named("picasso") OkHttpClient okHttpClient) {
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

    @Named("picasso")
    @Provides
    @Singleton
    public OkHttpClient providePicassoOkHttpClient(Optional<Cache> cache) {
        final OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        if (cache.isPresent()) {
            okHttpClient.cache(cache.get());
        }

        return okHttpClient.build();
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor,
                                            Optional<Cache> cache) {
        final OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        if (cache.isPresent()) {
            okHttpClient.cache(cache.get());
        }

        okHttpClient.interceptors().add(authInterceptor);

        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        okHttpClient.interceptors().add(loggingInterceptor);
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

    @Provides
    @Singleton
    Optional<Cache> provideCache(@ForApplication Context context) {
        final File httpCacheDir = new File(context.getCacheDir(), "cache");
        final long httpCacheSize = 100 * 1024 * 1024; // 100 MiB
        return Optional.of(new Cache(httpCacheDir, httpCacheSize));
    }

    @Singleton
    @Provides
    AuthInterceptor prvideAuthInterceptor(UserPreferences userPreferences) {
        return new AuthInterceptor(userPreferences);
    }

    @Singleton
    @Provides
    public ShoutsDao provideShoutsDao(ApiService apiService,
                                      @NetworkScheduler Scheduler networkScheduler,
                                      UserPreferences userPreferences) {
        return new ShoutsDao(apiService, networkScheduler, userPreferences);
    }

    @Singleton
    @Provides
    public DiscoversDao provideDiscoversDao(ApiService apiService,
                                            @NetworkScheduler Scheduler networkScheduler) {
        return new DiscoversDao(apiService, networkScheduler);
    }

    @Provides
    GoogleApiClient providesGoogleApiClient(@ForApplication Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    @Singleton
    @Provides
    public LocationManager provideLocationManager(@ForApplication Context context,
                                                  ApiService apiService,
                                                  UserPreferences userPreferences,
                                                  GoogleApiClient googleApiClient,
                                                  @NetworkScheduler Scheduler networkScheduler) {
        return new LocationManager(context, userPreferences, googleApiClient, apiService, networkScheduler);
    }

    @Singleton
    @Provides
    public DiscoverShoutsDao provideDiscoverShoutsDao(@NetworkScheduler Scheduler networkScheduler,
                                                      ApiService apiService) {
        return new DiscoverShoutsDao(networkScheduler, apiService);
    }

    @Singleton
    @Provides
    public ProfilesDao provideProfilesDao(ApiService apiService,
                                          @NetworkScheduler Scheduler networkScheduler,
                                          UserPreferences userPreferences) {
        return new ProfilesDao(apiService, networkScheduler, userPreferences);
    }

    @Singleton
    @Provides
    public TagsDao provideTagsDao(ApiService apiService,
                                  @NetworkScheduler Scheduler networkScheduler) {
        return new TagsDao(apiService, networkScheduler);
    }

    @Singleton
    @Provides
    public ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter() {
        return new ShoutsGlobalRefreshPresenter();
    }

    @Singleton
    @Provides
    public NotificationsDao notificationsDao(ApiService apiService,
                                             @NetworkScheduler Scheduler networkScheduler) {
        return new NotificationsDao(apiService, networkScheduler);
    }

    @Singleton
    @Provides
    public TransferUtility providesTransferUtility(@ForApplication Context context) {
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                AmazonConstants.AMAZON_S3_ID, AmazonConstants.AMAZON_S3_SECRET);

        final AmazonS3 s3Client = new AmazonS3Client(awsCredentials);
        s3Client.setRegion(Region.getRegion(Regions.EU_WEST_1));
        return new TransferUtility(s3Client, context);
    }

    @Provides
    ContentResolver provideContentResolver(@ForApplication Context context) {
        return context.getContentResolver();
    }

    @Provides
    @Singleton
    SuggestionsDao provideSuggestionsDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new SuggestionsDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    CategoriesDao categoriesDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new CategoriesDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    ListenersDaos listenersDaos(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new ListenersDaos(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    ListeningsDao listeningsDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new ListeningsDao(apiService, networkScheduler);
    }

    @Singleton
    @Provides
    public VideoCallsDao provideVideoCallsDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new VideoCallsDao(apiService, networkScheduler);
    }

    @Singleton
    @Provides
    ConversationMediaDaos provideConversationMediaDaos(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new ConversationMediaDaos(apiService, networkScheduler);
    }

    @Singleton
    @Provides
    public UsersIdentityDao provideUsersIdentityDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new UsersIdentityDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    SortTypesDao sortTypesDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new SortTypesDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    DbHelper dbHelper(@ForApplication Context context) {
        return new DbHelper(context);
    }

    @Provides
    @Singleton
    PusherHelper providePusher(Gson gson, UserPreferences userPreferences, @UiScheduler Scheduler uiScheduler) {
        return new PusherHelper(gson, userPreferences, uiScheduler);
    }

    @Provides
    @Singleton
    NetworkObservableProvider provideNetworkObservableProvider(@ForApplication Context context) {
        return new NetworkObservableProviderImpl(context);
    }

    @Provides
    CameraTool provideCameraTool(@Nonnull Lazy<CameraToolImpl> old, @Nonnull Lazy<CameraToolImplLollipop> lollipop) {
        if (VersionUtils.isAtLeastLollipop()) {
            return lollipop.get();
        } else {
            return old.get();
        }
    }

    @Provides
    SharedPreferences provideSharedPrefs(@ForApplication Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    FacebookHelper provideFacebookHelper(ApiService apiService, UserPreferences userPreferences,
                                         @ForApplication Context context, @NetworkScheduler Scheduler networkScheduler,
                                         PusherHelper pusherHelper) {
        return new FacebookHelper(apiService, userPreferences, context, networkScheduler, pusherHelper);
    }

    @Provides
    @Singleton
    LocalMessageBus provideLocalMessageBus() {
        return new LocalMessageBus();
    }

    @Provides
    @Singleton
    RefreshConversationBus provideRefreshConversationBus() {
        return new RefreshConversationBus();
    }
}