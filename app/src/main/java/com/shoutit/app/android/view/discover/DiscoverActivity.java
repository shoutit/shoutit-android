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
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class DiscoverActivity extends BaseActivity implements OnNewDiscoverSelectedListener {
    private static final String KEY_DISCOVER_ID = "discover_id";

    @Bind(R.id.activity_discover_toolbar)
    Toolbar toolbar;

    public static Intent newIntent(@Nonnull Context context, @Nonnull String discoverId) {
        return new Intent(context, DiscoverActivity.class)
                .putExtra(KEY_DISCOVER_ID, discoverId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        ButterKnife.bind(this);

        setUpActionBar();

        final Intent intent = checkNotNull(getIntent());
        final String discoverId = checkNotNull(intent.getStringExtra(KEY_DISCOVER_ID));

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
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
                return true;
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

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        final DiscoverActivityComponent component = DaggerDiscoverActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
