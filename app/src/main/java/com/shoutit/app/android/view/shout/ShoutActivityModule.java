package com.shoutit.app.android.view.shout;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

@Module
public class ShoutActivityModule {

    @Nonnull
    private final String shoutId;

    public ShoutActivityModule(ShoutActivity shoutActivity, @Nonnull String shoutId) {
        this.shoutId = shoutId;
    }

    @Provides
    String provideShoutId() {
        return shoutId;
    }
}
