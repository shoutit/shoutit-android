package com.shoutit.app.android.view.shouts;

import dagger.Module;
import dagger.Provides;

@Module
public class DiscoverShoutsActivityModule {

    private final String discoveryId;

    public DiscoverShoutsActivityModule(String discoveryId) {
        this.discoveryId = discoveryId;
    }

    @Provides
    public String getDiscoveryId() {
        return discoveryId;
    }
}
