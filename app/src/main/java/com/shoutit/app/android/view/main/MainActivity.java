package com.shoutit.app.android.view.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.appunite.appunitegcm.AppuniteGcm;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.model.Stats;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.BackPressedHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.KeyboardHelper;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.utils.PlayServicesHelper;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.view.discover.DiscoverActivity;
import com.shoutit.app.android.view.discover.OnNewDiscoverSelectedListener;
import com.shoutit.app.android.view.home.HomeFragment;
import com.shoutit.app.android.view.intro.IntroActivity;
import com.shoutit.app.android.view.postlogininterest.PostLoginInterestActivity;
import com.shoutit.app.android.view.search.main.MainSearchActivity;
import com.shoutit.app.android.view.signin.LoginActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends BaseActivity implements OnMenuItemSelectedListener,
        OnNewDiscoverSelectedListener, OnSeeAllDiscoversListener {

    private static final String MENU_SELECT_ITEM = "args_menu_item";
    private static final String TAG = MainActivity.class.getCanonicalName();

    public static final int REQUST_CODE_CAMERA_PERMISSION = 1;
    public static final int REQUST_CODE_CALL_PHONE_PERMISSION = 2;
    public static final int REQUST_CODE_PLAY_SERVICES_CHECK = 3;

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
    @Inject
    Twilio twilio;
    @Inject
    MixPanel mixPanel;
    @Inject
    PusherHelper mPusherHelper;
    @Inject
    ApiService apiService;
    @Inject
    DeepLinksHelper deepLinksHelper;

    private ActionBarDrawerToggle drawerToggle;
    private BackPressedHelper mBackPressedHelper;
    private final CompositeSubscription mStatsSubscription = new CompositeSubscription();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBackPressedHelper = new BackPressedHelper(this);

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

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_main_fragment_container, HomeFragment.newInstance())
                    .commit();
            menuHandler.initMenu(drawerLayout);
            registerToGcm();
        } else {
            final int selectedItem = savedInstanceState.getInt(MENU_SELECT_ITEM);
            menuHandler.initMenu(drawerLayout, selectedItem);
        }

        deepLinksHelper.checkForDeepLinksIntent(getIntent());

        if (mUserPreferences.isNormalUser()) {
            subscribeToStats();
        }

        mixPanel.initMixPanel(); // Workaround for mixpanel people id issue
        mixPanel.showNotificationIfAvailable(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        deepLinksHelper.checkForDeepLinksIntent(intent);
    }

    private void registerToGcm() {
        if (PlayServicesHelper.checkPlayServices(this, REQUST_CODE_PLAY_SERVICES_CHECK)) {
            profilesDao.registerToGcmAction(AppuniteGcm.getInstance()
                    .getPushToken());
        }
    }

    private void subscribeToStats() {
        mStatsSubscription.add(mPusherHelper.getStatsObservable()
                .compose(this.<Stats>bindToLifecycle())
                .subscribe(pusherStats -> {
                    menuHandler.setStats(pusherStats.getUnreadConversationsCount(), pusherStats.getUnreadNotifications());
                }));
        mStatsSubscription.add(mUserPreferences.getPageOrUserObservable()
                .filter(Functions1.isNotNull())
                .distinctUntilChanged()
                .compose(this.<BaseProfile>bindToLifecycle())
                .subscribe(user -> {
                    menuHandler.setStats(user.getUnreadConversationsCount(), user.getUnreadNotificationsCount());
                }, throwable -> {
                    LogHelper.logThrowable(TAG, "error", throwable);
                }));
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
            KeyboardHelper.hideSoftKeyboard(this);
            return true;
        }

        switch (item.getItemId()) {
            case R.id.base_menu_search:
                return showMainSearchActivityOrLetFragmentsHandleIt();
            case R.id.base_menu_chat:
                menuHandler.selectMenuItem(MenuHandler.FRAGMENT_CHATS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void changeMenuItem(String tag) {
        menuHandler.selectMenuItem(tag);
    }

    private boolean showMainSearchActivityOrLetFragmentsHandleIt() {
        final Fragment fragment = Iterables.getLast(getSupportFragmentManager().getFragments());
        if (fragment != null && MenuHandler.FRAGMENT_DISCOVER.equals(fragment.getTag())) {
            return false;
        } else {
            startActivity(MainSearchActivity.newIntent(this));
            return true;
        }
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
        if (!mUserPreferences.isNormalUser()) {
            if (MenuHandler.FRAGMENT_CHATS.equals(fragmentTag) || MenuHandler.FRAGMENT_CREDITS.equals(fragmentTag)) {
                {
                    startActivity(LoginActivity.newIntent(MainActivity.this));
                    return;
                }
            }
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);

        if (fragment == null) {
            fragment = MenuHandler.getFragmentForTag(fragmentTag);
        }

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
        mixPanel.flush();
        mStatsSubscription.unsubscribe();
        super.onDestroy();
        mBackPressedHelper.removeCallbacks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUST_CODE_PLAY_SERVICES_CHECK) {
            registerToGcm();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUST_CODE_CAMERA_PERMISSION || requestCode == REQUST_CODE_CALL_PHONE_PERMISSION) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                ColoredSnackBar.success(findViewById(android.R.id.content), R.string.permission_granted, Snackbar.LENGTH_SHORT).show();
            } else {
                ColoredSnackBar.error(findViewById(android.R.id.content), R.string.permission_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mBackPressedHelper.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
