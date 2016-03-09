package com.shoutit.app.android.view.createshout.edit;

import android.support.annotation.NonNull;

import javax.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

@Module
public class EditShoutActivityModule {

    @NonNull
    private final String shoutId;

    public EditShoutActivityModule(@NonNull String shoutId) {
        this.shoutId = shoutId;
    }

    @NonNull
    @Provides
    public String provideShoutId() {
        return shoutId;
    }
}
