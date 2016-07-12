package com.shoutit.app.android.view.profile.page.edit;

import android.support.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

@Module
public class EditPageActivityModule {

    @Nullable
    private final String username;
    private final boolean mNotLoggedIn;

    public EditPageActivityModule(@Nullable String username, boolean notLoggedIn) {
        this.username = username;
        mNotLoggedIn = notLoggedIn;
    }

    @Provides
    public PageDataProvider providePageDataProvider(PageDataProviderFactory factory) {
        if (mNotLoggedIn) {
            assert username != null;
            return factory.createRemotePageDataProvider(username);
        } else {
            return factory.createLocalPageDataProvider();
        }
    }

}
