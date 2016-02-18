package com.shoutit.app.android.view.main;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Observer;
import rx.subjects.PublishSubject;

@Module
public class MainActivityModule {

    @Nonnull
    private final MainActivity activity;

    public MainActivityModule(@Nonnull MainActivity activity) {
        this.activity = activity;
    }

    @Provides
    RxAppCompatActivity provideRxActivity() {
        return activity;
    }

    @Provides
    OnMenuItemSelectedListener provideOnMenuItemSelectedListener() {
        return activity;
    }

    @Provides
    OnNewDiscoverSelectedListener provideOnNewDiscoverSelectedListener() {
        return activity;
    }
}
