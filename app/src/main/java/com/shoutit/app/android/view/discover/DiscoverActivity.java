package com.shoutit.app.android.view.discover;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.utils.AppseeHelper;
import com.shoutit.app.android.utils.UpNavigationHelper;
import com.shoutit.app.android.view.conversations.ConversationsActivity;
import com.shoutit.app.android.view.signin.LoginActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class DiscoverActivity extends BaseDaggerActivity implements OnNewDiscoverSelectedListener {
    private static final String KEY_DISCOVER_ID = "discover_id";

    @Bind(R.id.activity_discover_toolbar)
    Toolbar toolbar;

    @Inject
    UserPreferences mUserPreferences;

    public static Intent newIntent(@Nonnull Context context, @Nonnull String discoverId) {
        return new Intent(context, DiscoverActivity.class)
                .putExtra(KEY_DISCOVER_ID, discoverId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        ButterKnife.bind(this);

        AppseeHelper.start(this);

        setUpActionBar();

        final Intent intent = checkNotNull(getIntent());
        String discoverId = intent.getStringExtra(KEY_DISCOVER_ID);
        if (discoverId == null && intent.getData() != null) {
            discoverId = intent.getData().getQueryParameter("id");
        }
        checkNotNull(discoverId);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_discover_fragment_container, DiscoverFragment.newInstance(discoverId))
                    .commit();
        }
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    new UpNavigationHelper(this).onUpButtonClicked();
                }
                return true;
            case R.id.base_menu_search:
                return false;
            case R.id.base_menu_chat:
                if (mUserPreferences.isNormalUser()) {
                    startActivity(ConversationsActivity.newIntent(DiscoverActivity.this));
                } else {
                    startActivity(LoginActivity.newIntent(DiscoverActivity.this));
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onNewDiscoverSelected(@Nonnull String discoverId) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(R.id.activity_discover_fragment_container, DiscoverFragment.newInstance(discoverId))
                .commit();
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
