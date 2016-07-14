package com.shoutit.app.android.view.profile.page.edit;


import android.support.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

@Module
public class EditPageActivityModule {

    @Nullable
    private final String pageUsername;

    public EditPageActivityModule(@Nullable String pageUsername) {
        this.pageUsername = pageUsername;
    }

    @Provides
    public String providePageUsername() {
        return pageUsername;
    }

}
