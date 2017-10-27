package com.shoutit.app.android.view.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.utils.AppseeHelper;

public class ProfileDeepLinkDispatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppseeHelper.start(this);

        final Intent intent = getIntent();
        final Uri data = intent.getData();
        final String type = data.getQueryParameter("type");

        final String username = data.getQueryParameter("username");
        final Intent profileIntent = ProfileIntentHelper.newIntent(this, username, BaseProfile.PAGE.equals(type))
                .setData(data);
        if (intent.getExtras() != null) {
            profileIntent.putExtras(intent.getExtras());
        }

        startActivity(profileIntent);

        finish();
    }
}
