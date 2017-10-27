package com.shoutit.app.android.view.settings.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.settings.account.email.ChangeEmailFragment;
import com.shoutit.app.android.view.settings.account.linkedaccounts.LinkedAccountsFragment;
import com.shoutit.app.android.view.settings.account.password.ChangePasswordFragment;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AccountActivity extends BaseActivity {

    public static final String FRAGMENT_CHANGE_EMAIL = "change_email";
    public static final String FRAGMENT_CHANGE_PASSWORD = "change_password";
    public static final String FRAGMENT_LINKED_ACCOUNTS = "linked_accounts";

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, AccountActivity.class);
    }

    @Bind(R.id.activity_account_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);

        setUpToolbar();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.account_fragment_container, AccountFragment.newInstance())
                    .commit();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    setActionBarTitle(R.string.account_title);
                }
            }
        });
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.account_title);
    }

    private void setActionBarTitle(@StringRes int titleResId) {
        getSupportActionBar().setTitle(titleResId);
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

    public void onFragmentSelected(@Nonnull String fragmentTag) {
        int titleId;

        switch (fragmentTag){
            case FRAGMENT_CHANGE_EMAIL:
                titleId = R.string.account_title_change_email;
                break;
            case FRAGMENT_CHANGE_PASSWORD:
                titleId = R.string.account_title_change_password;
                break;
            case FRAGMENT_LINKED_ACCOUNTS:
                titleId = R.string.account_title_linked_accounts;
                break;
            default:
                throw new RuntimeException("Unknown fragment tag");
        }
        setActionBarTitle(titleId);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right)
                .addToBackStack(null)
                .replace(R.id.account_fragment_container, getFragmentForTag(fragmentTag), fragmentTag)
                .commit();
    }

    private Fragment getFragmentForTag(@Nonnull String fragmentTag) {
        switch (fragmentTag) {
            case FRAGMENT_CHANGE_EMAIL:
                return ChangeEmailFragment.newInstance();
            case FRAGMENT_CHANGE_PASSWORD:
                return ChangePasswordFragment.newInstance();
            case FRAGMENT_LINKED_ACCOUNTS:
                return LinkedAccountsFragment.newInstance();
            default:
                throw new RuntimeException("Unknown fragment type");
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Fragment linkedAccFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LINKED_ACCOUNTS);
        if (linkedAccFragment != null) linkedAccFragment.onActivityResult(requestCode,resultCode,data);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        final AccountActivityComponent component = DaggerAccountActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
