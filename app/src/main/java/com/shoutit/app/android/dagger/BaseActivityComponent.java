package com.shoutit.app.android.dagger;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
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

    Picasso getPicasso();

    LayoutInflater getLayoutInflater();

    ApiService apiService();
}