package com.shoutit.app.android.view.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.shoutit.app.android.api.model.BaseProfile;

public class ProfileDeepLinkDispatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final Uri data = intent.getData();
        final String type = data.getQueryParameter("type");

        final String username = data.getQueryParameter("username");
        startActivity(ProfileIntentHelper.newIntent(this, username, BaseProfile.PAGE.equals(type)).setData(data));

        finish();
    }
}
