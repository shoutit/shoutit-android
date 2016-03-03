package com.shoutit.app.android.view.createshout.edit;

import javax.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

@Module
public class EditShoutActivityModule {

    @Nullable
    private final String shoutId;

    public EditShoutActivityModule(@Nullable String shoutId) {
        this.shoutId = shoutId;
    }

    @Nullable
    @Provides
    public String provideShoutId() {
        return shoutId;
    }
}
