package com.shoutit.app.android.mixpanel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.appunite.rx.functions.Functions1;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.BuildTypeUtils;
import com.shoutit.app.android.utils.LogHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class MixPanel {
    private static final String TAG = MixPanel.class.getSimpleName();

    private static final String PRODUCTION = "c9d0a1dc521ac1962840e565fa971574";
    private static final String DEVELOPMENT = "d2de0109a8de7237dede66874c7b8951";
    private static final String LOCAL = "a5774a99b9068ae66129859421ade687";
    private static final String API_CLIENT = "shoutit-android";

    private static final String PEOPLE_FIELD_USER_NAME = "username";
    private static final String PEOPLE_FIELD_EMAIL = "Email";
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

    private final Pattern UTM_SOURCE_PATTERN = Pattern.compile("(^|&)utm_source=([^&#=]*)([#&]|$)");
    private final Pattern UTM_MEDIUM_PATTERN = Pattern.compile("(^|&)utm_medium=([^&#=]*)([#&]|$)");
    private final Pattern UTM_CAMPAIGN_PATTERN = Pattern.compile("(^|&)utm_campaign=([^&#=]*)([#&]|$)");
    private final Pattern UTM_CONTENT_PATTERN = Pattern.compile("(^|&)utm_content=([^&#=]*)([#&]|$)");
    private final Pattern UTM_TERM_PATTERN = Pattern.compile("(^|&)utm_term=([^&#=]*)([#&]|$)");

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
        userPreferences.getUserObservable()
                .filter(Functions1.isNotNull())
                .distinctUntilChanged()
                .subscribe(user -> {
                    mixpanel.identify(user.getId());
                    mixpanel.getPeople().identify(user.getId());
                    mixpanel.getPeople().set(PEOPLE_FIELD_USER_NAME, user.getUsername());
                    mixpanel.getPeople().set(PEOPLE_FIELD_EMAIL, user.getEmail());
                });
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

    public void trackAppOpenOrClose(boolean appOpen, @Nullable final Map<String, String> params) {
        LogHelper.logIfDebug(TAG, "app open event: " + appOpen);

        final boolean isLoggedIn = userPreferences.isUserLoggedIn();
        final boolean isGuest = userPreferences.isGuest();

        final JSONObject properties = new JSONObject();
        try {
            if (params != null && appOpen) {
                for (String key : params.keySet()) {
                    properties.put(key, params.get(key));
                }
                userPreferences.mixpanelCampaignParamsObserver().onNext(null);
            }
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
        mixpanel.getPeople().showNotificationIfAvailable(activity);
    }

    public void utmParamsFromIntent(@Nonnull final Intent intent) {
        final Map<String, String> params = new HashMap<>();

        if (intent.getData() != null && !intent.getData().getQueryParameterNames().isEmpty()) {
            final Uri uri = intent.getData();
            for (String parameter : uri.getQueryParameterNames()) {
                if (parameter.startsWith("utm_")) {
                    params.put(parameter, uri.getQueryParameter(parameter));
                }
            }
        }

        if (intent.getExtras() != null && intent.hasExtra("referrer")) {
            final String referrer = intent.getStringExtra("referrer");
            params.put("referrer", referrer);

            final Matcher sourceMatcher = UTM_SOURCE_PATTERN.matcher(referrer);
            final String source = find(sourceMatcher);
            if (null != source) {
                params.put("utm_source", source);
            }

            final Matcher mediumMatcher = UTM_MEDIUM_PATTERN.matcher(referrer);
            final String medium = find(mediumMatcher);
            if (null != medium) {
                params.put("utm_medium", medium);
            }

            final Matcher campaignMatcher = UTM_CAMPAIGN_PATTERN.matcher(referrer);
            final String campaign = find(campaignMatcher);
            if (null != campaign) {
                params.put("utm_campaign", campaign);
            }

            final Matcher contentMatcher = UTM_CONTENT_PATTERN.matcher(referrer);
            final String content = find(contentMatcher);
            if (null != content) {
                params.put("utm_content", content);
            }

            final Matcher termMatcher = UTM_TERM_PATTERN.matcher(referrer);
            final String term = find(termMatcher);
            if (null != term) {
                params.put("utm_term", term);
            }
        }
        if (!params.isEmpty()) {
            userPreferences.mixpanelCampaignParamsObserver().onNext(params);
        }
    }

    private String find(Matcher matcher) {
        if (matcher.find()) {
            final String encoded = matcher.group(2);
            if (null != encoded) {
                try {
                    return URLDecoder.decode(encoded, "UTF-8");
                } catch (final UnsupportedEncodingException ignore) {}
            }
        }
        return null;
    }
}
