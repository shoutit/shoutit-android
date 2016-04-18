package com.shoutit.app.android.utils;

import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.shoutit.app.android.R;

public class BackPressedHelper {

    private final Handler backButtonHandler = new Handler();
    private final AppCompatActivity mActivity;
    private boolean doubleBackToExitPressedOnce;
    private final Runnable backButtonRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    public BackPressedHelper(AppCompatActivity activity) {
        mActivity = activity;
    }

    public boolean onBackPressed() {
        if (doubleBackToExitPressedOnce || mActivity.getSupportFragmentManager().getBackStackEntryCount() != 0) {
            return false;
        }

        doubleBackToExitPressedOnce = true;
        Snackbar.make(mActivity.findViewById(android.R.id.content), R.string.exit_text, Snackbar.LENGTH_SHORT).show();

        backButtonHandler.postDelayed(backButtonRunnable, 2000);
        return true;
    }

    public void removeCallbacks(){
        backButtonHandler.removeCallbacks(backButtonRunnable);
    }

}
