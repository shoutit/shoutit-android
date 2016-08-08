package com.shoutit.app.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.BuildTypeUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppPreferences {

    private static final String PREF_NAME = "AppPrefs";

    private static final String KEY_APP_OPENINGS = "app_openings";
    private static final String KEY_SHOUTS_CREATED_NUM = "shouts_created_num";
    private static final String KEY_WAS_RATE_DIALOG_SHOWN = "was_rate_dialog_shown";

    private static final int APP_OPENS_TO_SHOW_RATE_DIALOG = BuildTypeUtils.isDebug() ? 4 : 10;
    private static final int SHOUT_CREATE_NUM_TO_SHOW_RATE_DIALOG = BuildTypeUtils.isDebug() ? 1 : 3;

    private final SharedPreferences sharedPreferences;

    @Inject
    public AppPreferences(@ForApplication Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
    }

    public boolean shouldShowRateDialogForAppOpening() {
        final boolean wasRateDialogShown = sharedPreferences.getBoolean(KEY_WAS_RATE_DIALOG_SHOWN, false);
        return !wasRateDialogShown && sharedPreferences.getInt(KEY_APP_OPENINGS, 0) >= APP_OPENS_TO_SHOW_RATE_DIALOG;
    }

    public boolean shouldShowRateDialogForShoutCreate() {
        final boolean wasRateDialogShown = sharedPreferences.getBoolean(KEY_WAS_RATE_DIALOG_SHOWN, false);
        return !wasRateDialogShown && sharedPreferences.getInt(KEY_SHOUTS_CREATED_NUM, 0) >= SHOUT_CREATE_NUM_TO_SHOW_RATE_DIALOG;
    }

    @SuppressLint("CommitPrefEdits")
    public void increaseAppOpenings() {
        sharedPreferences.edit()
                .putInt(KEY_APP_OPENINGS, sharedPreferences.getInt(KEY_APP_OPENINGS, 0) + 1)
                .commit();
    }

    @SuppressLint("CommitPrefEdits")
    public void increaseCreatedShouts() {
        sharedPreferences.edit()
                .putInt(KEY_SHOUTS_CREATED_NUM, sharedPreferences.getInt(KEY_SHOUTS_CREATED_NUM, 0) + 1)
                .commit();
    }

    @SuppressLint("CommitPrefEdits")
    public void setRateDialogShown() {
        sharedPreferences.edit()
                .putBoolean(KEY_WAS_RATE_DIALOG_SHOWN, true)
                .commit();
    }

    public void resetCounters() {
        sharedPreferences.edit()
                .putInt(KEY_APP_OPENINGS, 0)
                .putInt(KEY_SHOUTS_CREATED_NUM, 0)
                .apply();
    }
}
