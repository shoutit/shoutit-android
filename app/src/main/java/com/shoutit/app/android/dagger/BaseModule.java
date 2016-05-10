package com.shoutit.app.android.dagger;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.schedulers.Schedulers;

@Module
public final class BaseModule {

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Provides
    @UiScheduler
    Scheduler provideUiScheduler() {
        return MyAndroidSchedulers.mainThread();
    }

    @Provides
    @NetworkScheduler
    Scheduler provideNetworkScheduler() {
        return Schedulers.io();
    }
}