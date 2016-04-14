package com.shoutit.app.android.view.createshout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.view.createshout.request.CreateRequestActivity;
import com.shoutit.app.android.view.createshout.request.CreateShoutDialogActivityComponent;
import com.shoutit.app.android.view.createshout.request.DaggerCreateShoutDialogActivityComponent;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.media.RecordMediaActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateShoutDialogActivity extends BaseActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, CreateShoutDialogActivity.class);
    }

    @Inject
    UserPreferences mUserPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_shout_dialog);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.create_shout_offer)
    public void createShout() {
        if (mUserPreferences.isGuest()) {
            startActivity(LoginIntroActivity.newIntent(this));
        } else {
            if (PermissionHelper.checkPermissions(this,
                    MainActivity.REQUST_CODE_CAMERA_PERMISSION,
                    ColoredSnackBar.contentView(this),
                    R.string.permission_camera_explanation,
                    new String[] {Manifest.permission.CAMERA})) {
                startActivity(RecordMediaActivity.newIntent(this, false, false, false, true));
            }
        }
        finish();
    }

    @OnClick(R.id.create_shout_request)
    public void createRequest() {
        if (mUserPreferences.isGuest()) {
            startActivity(LoginIntroActivity.newIntent(this));
        } else {
            startActivity(CreateRequestActivity.newIntent(this));
        }
        finish();
    }

    @OnClick(R.id.dialog_background)
    public void clickOnDialogBackground() {
        finish();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final CreateShoutDialogActivityComponent component = DaggerCreateShoutDialogActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();

        component.inject(this);
        return component;
    }
}
