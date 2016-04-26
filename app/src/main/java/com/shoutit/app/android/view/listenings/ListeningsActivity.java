package com.shoutit.app.android.view.listenings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
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

import static com.google.common.base.Preconditions.checkNotNull;

public class ListeningsActivity extends BaseActivity {

    private static final String KEY_ARE_INTERESTS = "are_interests";

    @Bind(R.id.listening_tab_layout)
    TabLayout tabLayout;
    @Bind(R.id.listening_view_pager)
    ViewPager viewPager;
    @Bind(R.id.listenings_toolbar)
    Toolbar toolbar;

    @Inject
    ListeningsPagerAdapter pagerAdapter;

    public static Intent newIntent(Context context, boolean areInterests) {
        return new Intent(context, ListeningsActivity.class)
                .putExtra(KEY_ARE_INTERESTS, areInterests);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listenings);
        ButterKnife.bind(this);

        final boolean areInterests = checkNotNull(getIntent().getBooleanExtra(KEY_ARE_INTERESTS, false));

        if (areInterests) {
            viewPager.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.listenings_container,
                            ListeningsFragment.newInstance(ListeningsPresenter.ListeningsType.TAGS))
                    .commit();
        } else {
            viewPager.setAdapter(pagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
        }

        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.listenings_ab_title);
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
        final ListeningsActivityComponent component = DaggerListeningsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
