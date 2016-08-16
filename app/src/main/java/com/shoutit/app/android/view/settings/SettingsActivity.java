package com.shoutit.app.android.view.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.utils.UpNavigationHelper;
import com.shoutit.app.android.view.about.AboutActivity;
import com.shoutit.app.android.view.settings.account.AccountActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends BaseDaggerActivity {

    @Bind(R.id.activity_settings_toolbar)
    Toolbar toolbar;
    @Bind(R.id.settings_account)
    TextView accountTextView;
    @Bind(R.id.settings_account_divider)
    View accountDivider;

    @Inject
    UserPreferences userPreferences;

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setUpToolbar();

        accountTextView.setVisibility(userPreferences.isGuest() ? View.GONE : View.VISIBLE);
        accountDivider.setVisibility(userPreferences.isGuest() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                new UpNavigationHelper(this).onUpButtonClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        startActivity(IntentHelper.getAppSettingsIntent(this));
    }

    @OnClick(R.id.settings_account)
    public void onAccountClick() {
        startActivity(AccountActivity.newIntent(this));
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
