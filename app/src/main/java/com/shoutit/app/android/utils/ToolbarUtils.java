package com.shoutit.app.android.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;

import com.shoutit.app.android.R;

public class ToolbarUtils {

    public static void setupToolbar(@NonNull Toolbar toolbar, @StringRes int title, @NonNull Activity activity) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(view -> activity.finish());
    }

}
