package com.shoutit.app.android.view.shouts;

import dagger.Module;
import dagger.Provides;

@Module
public class ShoutsActivityModule {

    private final String discoveryId;

    public ShoutsActivityModule(String discoveryId) {
        this.discoveryId = discoveryId;
    }

    @Provides
    public String getDiscoveryId() {
        return discoveryId;
    }
}
