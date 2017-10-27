package com.shoutit.app.android.view.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.data.DeepLinksContants;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.view.discover.DiscoverActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DeepLinksHelper {

    private final MenuHandler menuHandler;
    @Nonnull
    private final Context context;
    @Nonnull
    private final MixPanel mixPanel;

    @Inject
    public DeepLinksHelper(@Nonnull MenuHandler menuHandler,
                           @Nonnull @ForActivity Context context,
                           @Nonnull MixPanel mixPanel) {
        this.menuHandler = menuHandler;
        this.context = context;
        this.mixPanel = mixPanel;
    }

    public void checkForDeepLinksIntent(Intent intent) {
        final Uri uri = intent.getData();
        if (uri == null) {
            return;
        }
        final String stringUri = uri.toString();
        mixPanel.utmParamsFromIntent(intent);

        if (stringUri.contains(DeepLinksContants.HOME)) {
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_HOME);
        } else if (stringUri.contains(DeepLinksContants.BROWSE)) {
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_BROWSE);
        } else if (stringUri.contains(DeepLinksContants.DISCOVER_ITEM)) {
            final String id = uri.getQueryParameter("id");
            context.startActivity(DiscoverActivity.newIntent(context, id));
        } else if (stringUri.contains(DeepLinksContants.DISCOVER)) {
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_DISCOVER);
        } else if (stringUri.contains(DeepLinksContants.PUBLIC_CHATS)) {
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_PUBLIC_CHATS);
        } else if (stringUri.contains(DeepLinksContants.CHATS)) {
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_CHATS);
        }
    }

    public static boolean isFromDeeplink(Intent intent) {
        return intent.getData() != null;
    }
}
