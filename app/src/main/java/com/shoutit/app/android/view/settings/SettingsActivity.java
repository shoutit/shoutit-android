package com.shoutit.app.android.view.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.about.AboutActivity;
import com.shoutit.app.android.view.settings.account.AccountActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    @Bind(R.id.activity_settings_toolbar)
    Toolbar toolbar;
    @Bind(R.id.settings_version_name)
    TextView versionTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setUpToolbar();

        versionTextView.setText(getString(R.string.menu_version_name, BuildConfig.VERSION_NAME));
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.settings_title);
    }

    @OnClick(R.id.settings_about)
    public void onAboutClick() {
        startActivity(AboutActivity.newIntent(this));
    }

    @OnClick(R.id.settings_notifications)
    public void onNotificationsClick() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.settings_account)
    public void onAccountClick() {
        startActivity(AccountActivity.newIntent(this));
    }
}
