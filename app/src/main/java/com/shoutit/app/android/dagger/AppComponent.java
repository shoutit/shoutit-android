package com.shoutit.app.android.dagger;

import android.content.Context;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.App;
import com.shoutit.app.android.api.ApiService;
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

    ApiService getApiService();
}