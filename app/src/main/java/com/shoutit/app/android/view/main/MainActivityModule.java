package com.shoutit.app.android.view.main;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityScope;
import com.squareup.picasso.Picasso;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

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
    @ActivityScope
    MenuHandler providesMenuHandler(OnMenuItemSelectedListener onMenuItemSelectedListener,
                                    Picasso picasso, UserPreferences userPreferences,
                                    MenuHandlerPresenter presenter) {
        return new MenuHandler(activity, onMenuItemSelectedListener, picasso, userPreferences, presenter);
    }

}
