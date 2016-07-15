package com.shoutit.app.android.view.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.view.profile.page.PageProfileActivity;
import com.shoutit.app.android.view.profile.user.UserProfileActivity;

public class ProfileDeepLinkDispatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final Uri data = intent.getData();
        final String type = data.getQueryParameter("type");
        final String lastPathSegment = data.getLastPathSegment();
        if (BaseProfile.PAGE.equals(type)) {
            startActivity(PageProfileActivity.newIntent(this, lastPathSegment));
        } else {
            startActivity(UserProfileActivity.newIntent(this, lastPathSegment));
        }
        finish();
    }
}
