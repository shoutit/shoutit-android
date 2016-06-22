package com.shoutit.app.android.mixpanel;

import android.app.Activity;
import android.content.Context;

import com.appunite.rx.functions.Functions1;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.BuildTypeUtils;
import com.shoutit.app.android.utils.LogHelper;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MixPanel {
    private static final String TAG = MixPanel.class.getSimpleName();

    private static final String PRODUCTION = "c9d0a1dc521ac1962840e565fa971574";
    private static final String DEVELOPMENT = "d2de0109a8de7237dede66874c7b8951";
    private static final String LOCAL = "a5774a99b9068ae66129859421ade687";
    private static final String API_CLIENT = "shoutit-android";

    /**
     * EVENTS
     **/
    private static final String EVENT_APP_OPEN = "app_open";
    private static final String EVENT_APP_CLOSE = "app_close";

    /**
     * PROPERTIES
     **/
    private static final String PROPERTY_SIGNED_USER = "signed_user";
    private static final String PROPERTY_IS_GUEST = "is_guest";
    private static final String PROPERTY_API_CLIENT = "api_client";

    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final MixpanelAPI mixpanel;

    @Inject
    public MixPanel(@ForApplication Context context, @Nonnull UserPreferences userPreferences) {
        mixpanel = MixpanelAPI.getInstance(context, getToken());
        this.userPreferences = userPreferences;
    }

    public void initMixPanel() {
        if (userPreferences.isNormalUser()) {
            userPreferences.getUserObservable()
                    .filter(Functions1.isNotNull())
                    .distinctUntilChanged()
                    .subscribe(user -> {
                        identifyMixPanel(user.getId());
                    });
        } else {
            final User guestUser = userPreferences.getGuestUser();
            if (guestUser != null) {
                identifyMixPanel(guestUser.getId());
            }
        }
    }

    private void identifyMixPanel(@Nonnull String userId) {
        mixpanel.identify(userId);
        mixpanel.getPeople().identify(userId);
    }

    private String getToken() {
        if (BuildTypeUtils.isRelease()) {
            return PRODUCTION;
        } else if (BuildTypeUtils.isStagingOrDebug()) {
            return DEVELOPMENT;
        } else if (BuildTypeUtils.isLocal()) {
            return LOCAL;
        } else {
            throw BuildTypeUtils.unknownTypeException();
        }
    }

    public void flush() {
        mixpanel.flush();
    }

    @Nonnull
    public String getDistinctId() {
        return mixpanel.getDistinctId();
    }

    public void trackAppOpenOrClose(boolean appOpen) {
        LogHelper.logIfDebug(TAG, "app open event: " + appOpen);

        final boolean isLoggedIn = userPreferences.isUserLoggedIn();
        final boolean isGuest = userPreferences.isGuest();

        final JSONObject properties = new JSONObject();
        try {
            properties.put(PROPERTY_SIGNED_USER, isLoggedIn);
            if (isLoggedIn) {
                properties.put(PROPERTY_IS_GUEST, isGuest);
            }
            properties.put(PROPERTY_API_CLIENT, API_CLIENT);
        } catch (JSONException e) {
            logError(e);
        }
        mixpanel.track(appOpen ? EVENT_APP_OPEN : EVENT_APP_CLOSE, properties);
    }

    private void logError(Throwable throwable) {
        LogHelper.logThrowable(TAG, "Cannot add property to json", throwable);
    }

    public void showNotificationIfAvailable(@Nonnull Activity activity) {
        if (mixpanel.getPeople().getDistinctId() != null) {
            mixpanel.getPeople().showNotificationIfAvailable(activity);
        } else {
            LogHelper.logIfDebug(TAG, "Mixpanel people distinctId is null");
        }
    }
}
