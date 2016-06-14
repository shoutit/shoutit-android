package com.shoutit.app.android.view.promote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PromoteShoutActivity extends BaseActivity {

    private static final String KEY_SHOUT_NAME = "shout_name";

    @Bind(R.id.promote_toolbar)
    Toolbar toolbar;
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.promote_recycler_view)
    RecyclerView recyclerView;
    
    @Inject
    PromoteAdapter adapter;
    @Inject
    PromotePresenter presenter;

    public static Intent newIntent(Context context, @Nonnull String shoutName) {
        return new Intent(context, PromoteShoutActivity.class)
                .putExtra(KEY_SHOUT_NAME, shoutName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promote_shout);
        ButterKnife.bind(this);

        setUpToolbar();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.promote_ab_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final PromoteShoutActivityComponent component = DaggerPromoteShoutActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
