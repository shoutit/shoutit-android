package com.shoutit.app.android.dagger;

import android.content.Context;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.gson.Gson;
import com.shoutit.app.android.App;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.view.signin.CoarseLocationObservableProvider;
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

    CoarseLocationObservableProvider coarseLocationObservableProvider();

    OkHttpClient getOkHttpClient();

    Gson gson();

    ApiService getApiService();

    ShoutsDao shoutsDao();

    DiscoversDao discoversDao();
}