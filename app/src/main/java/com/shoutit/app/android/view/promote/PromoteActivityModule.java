package com.shoutit.app.android.view.promote;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

@Module
public class PromoteActivityModule {

    @Nonnull
    private final String shoutTitle;

    public PromoteActivityModule(@Nonnull String shoutTitle) {
        this.shoutTitle = shoutTitle;
    }

    @Provides
    String provideShoutTitle() {
        return shoutTitle;
    }
}
