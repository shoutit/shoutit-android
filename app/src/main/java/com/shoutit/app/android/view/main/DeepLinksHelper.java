package com.shoutit.app.android.view.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.data.DeepLinksContants;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.view.createshout.request.CreateRequestActivity;
import com.shoutit.app.android.view.discover.DiscoverActivity;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.media.RecordMediaActivity;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DeepLinksHelper {

    private final MenuHandler menuHandler;
    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final Context context;
    @Nonnull
    private final MixPanel mixPanel;

    @Inject
    public DeepLinksHelper(@Nonnull MenuHandler menuHandler,
                           @Nonnull UserPreferences userPreferences,
                           @Nonnull @ForActivity Context context,
                           @Nonnull MixPanel mixPanel) {
        this.menuHandler = menuHandler;
        this.userPreferences = userPreferences;
        this.context = context;
        this.mixPanel = mixPanel;
    }

    public void checkForDeepLinksIntent(Intent intent) {
        final Uri uri = intent.getData();
        if (uri == null) {
            return;
        }
        final String stringUri = uri.toString();
        mixPanel.utmParamsFromUri(uri);

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
        } else if (stringUri.contains(DeepLinksContants.CREATE_SHOUT)) {
            if (userPreferences.isGuest()) {
                context.startActivity(LoginIntroActivity.newIntent(context));
                return;
            }

            final String type = uri.getQueryParameter("type");
            if (DeepLinksContants.CREATE_SHOUT_TYPE_OFFER.equals(type)) {
                context.startActivity(RecordMediaActivity.newIntent(context, false, false, false, true, true));
            } else if (DeepLinksContants.CREATE_SHOUT_TYPE_REQUEST.equals(type)) {
                context.startActivity(CreateRequestActivity.newIntent(context));
            }
        }
    }
}
