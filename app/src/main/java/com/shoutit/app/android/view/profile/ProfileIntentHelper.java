package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.Intent;

import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.view.profile.page.PageProfileActivity;
import com.shoutit.app.android.view.profile.user.UserProfileActivity;

public class ProfileIntentHelper {

    public static Intent newIntent(Context context, BaseProfile baseProfile) {
        return newIntent(context, baseProfile.getUsername(), baseProfile.isPage());
    }

    public static Intent newIntent(Context context, String username, boolean isPage) {
        if (isPage) {
            return PageProfileActivity.newIntent(context, username);
        } else {
            return UserProfileActivity.newIntent(context, username);
        }
    }

}
