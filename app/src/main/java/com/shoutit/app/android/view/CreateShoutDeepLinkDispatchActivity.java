package com.shoutit.app.android.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.dagger.DaggerBaseDaggerActivityComponent;
import com.shoutit.app.android.data.DeepLinksContants;
import com.shoutit.app.android.utils.AppseeHelper;
import com.shoutit.app.android.view.createshout.request.CreateRequestActivity;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.media.RecordMediaActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class CreateShoutDeepLinkDispatchActivity extends BaseActivity {

    @Inject
    UserPreferences userPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppseeHelper.start(this);

        if (!userPreferences.isNormalUser()) {
            startActivity(LoginIntroActivity.newIntent(this));
            finish();
            return;
        }

        final Intent intent = getIntent();
        final String type = intent.getData().getQueryParameter("type");

        if (DeepLinksContants.CREATE_SHOUT_TYPE_OFFER.equals(type)) {
            startActivity(RecordMediaActivity.newIntent(this, false, false, false, true, true));
        } else if (DeepLinksContants.CREATE_SHOUT_TYPE_REQUEST.equals(type)) {
            startActivity(CreateRequestActivity.newIntent(this));
        }

        finish();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        final BaseDaggerActivityComponent component = DaggerBaseDaggerActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
