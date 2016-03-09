package com.shoutit.app.android.view.main;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.view.discover.DiscoverActivity;
import com.shoutit.app.android.view.discover.OnNewDiscoverSelectedListener;
import com.shoutit.app.android.view.home.HomeFragment;
import com.shoutit.app.android.view.intro.IntroActivity;
import com.shoutit.app.android.view.postlogininterest.PostLoginInterestActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class MainActivity extends BaseActivity implements OnMenuItemSelectedListener,
        OnNewDiscoverSelectedListener, OnSeeAllDiscoversListener {

    private static final String MENU_SELECT_ITEM = "args_menu_item";

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Bind(R.id.activity_main_toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_drawer_layout)
    DrawerLayout drawerLayout;

    @Inject
    MenuHandler menuHandler;
    @Inject
    UserPreferences mUserPreferences;
    @Inject
    ProfilesDao profilesDao;

    private ActionBarDrawerToggle drawerToggle;
    private boolean doubleBackToExitPressedOnce;
    private final Handler backButtonHandler = new Handler();
    private final Runnable backButtonRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (!mUserPreferences.isUserLoggedIn() && !mUserPreferences.isGuest()) {
            finish();
            startActivity(IntroActivity.newIntent(this));
            return;
        }

        if (mUserPreferences.shouldAskForInterestAndSetToFalse()) {
            finish();
            startActivity(PostLoginInterestActivity.newIntent(this));
            return;
        }

        setUpActionBar();
        setUpDrawer();
        updateUser();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_main_fragment_container, HomeFragment.newInstance())
                    .commit();
            menuHandler.initMenu(drawerLayout);
        } else {
            final int selectedItem = savedInstanceState.getInt(MENU_SELECT_ITEM);
            menuHandler.initMenu(drawerLayout, selectedItem);
        }
    }

    private void setUpDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUser() {
        if (!mUserPreferences.isNormalUser()) {
            return;
        }

        profilesDao.updateUser()
                .compose(this.<User>bindToLifecycle())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        mUserPreferences.saveUserAsJson(user);
                    }
                });
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle == null) {
            return;
        }
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        final MainActivityComponent component = DaggerMainActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .mainActivityModule(new MainActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }

    @Override
    public void onMenuItemSelected(@Nonnull String fragmentTag) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);

        if (fragment == null) {
            fragment = MenuHandler.getFragmentForTag(fragmentTag);
        }

        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        fragmentManager.beginTransaction()
                .replace(R.id.activity_main_fragment_container, fragment, fragmentTag)
                .commit();

        drawerLayout.closeDrawers();
    }

    @Override
    public void onNewDiscoverSelected(@Nonnull String discoverId) {
        startActivity(DiscoverActivity.newIntent(this, discoverId));
    }

    @Override
    public void onSeeAllDiscovers() {
        menuHandler.setDiscoverMenuItem();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MENU_SELECT_ITEM, menuHandler.getSelectedItem());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backButtonHandler.removeCallbacks(backButtonRunnable);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce || getSupportFragmentManager().getBackStackEntryCount() != 0) {
            super.onBackPressed();
            return;
        }

        doubleBackToExitPressedOnce = true;
        Snackbar.make(findViewById(android.R.id.content), R.string.exit_text, Snackbar.LENGTH_SHORT).show();

        backButtonHandler.postDelayed(backButtonRunnable, 2000);
    }
}
