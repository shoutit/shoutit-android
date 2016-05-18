package com.shoutit.app.android.view.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.data.DeepLinksContants;
import com.shoutit.app.android.view.createshout.request.CreateRequestActivity;
import com.shoutit.app.android.view.discover.DiscoverActivity;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.media.RecordMediaActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DeepLinksHelper {

    private final MenuHandler menuHandler;
    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final Context context;

    @Inject
    public DeepLinksHelper(@Nonnull MenuHandler menuHandler,
                           @Nonnull UserPreferences userPreferences,
                           @Nonnull @ForActivity Context context) {
        this.menuHandler = menuHandler;
        this.userPreferences = userPreferences;
        this.context = context;
    }

    public void checkForDeepLinksIntent(Intent intent) {
        final Uri uri = intent.getData();
        if (uri == null) {
            return;
        }
        final String stringUri = uri.toString();

        if (stringUri.contains(DeepLinksContants.HOME)) {
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_HOME);
        } else if (stringUri.contains(DeepLinksContants.BROWSE)) {
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_BROWSE);
        } else if (stringUri.contains(DeepLinksContants.DISCOVER_ITEM)) {
            final String id = uri.getQueryParameter("id");
            context.startActivity(DiscoverActivity.newIntent(context, id));
        } else if (stringUri.contains(DeepLinksContants.DISCOVER)) {
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_DISCOVER);
        } else if (stringUri.contains(DeepLinksContants.CHATS)) {
            redirectToLoginIfGuest();
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_PUBLIC_CHATS);
        } else if (stringUri.contains(DeepLinksContants.PUBLIC_CHATS)) {
            redirectToLoginIfGuest();
            menuHandler.selectMenuItem(MenuHandler.FRAGMENT_CHATS);
        } else if (stringUri.contains(DeepLinksContants.CREATE_SHOUT)) {
            redirectToLoginIfGuest();
            final String type = uri.getQueryParameter("type");
            if (DeepLinksContants.CREATE_SHOUT_TYPE_OFFER.equals(type)) {
                context.startActivity(RecordMediaActivity.newIntent(context, false, false, false, true));
            } else if (DeepLinksContants.CREATE_SHOUT_TYPE_REQUEST.equals(type)) {
                context.startActivity(CreateRequestActivity.newIntent(context));
            }
        }
    }

    public void redirectToLoginIfGuest() {
        if (userPreferences.isGuest()) {
            context.startActivity(LoginIntroActivity.newIntent(context));
        }
    }
}
