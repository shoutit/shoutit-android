package com.shoutit.app.android.view.invitefriends.facebookfriends;

import android.os.Bundle;

import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profileslist.BaseProfilesListActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FacebookFriendsActivity extends BaseProfilesListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setUpToolbar() {

    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        return null;
    }
}
