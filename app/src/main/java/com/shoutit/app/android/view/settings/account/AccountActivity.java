package com.shoutit.app.android.view.settings.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import javax.annotation.Nonnull;

public class AccountActivity extends BaseActivity {

    public static final String FRAGMENT_CHANGE_EMAIL = "change_email";
    public static final String FRAGMENT_CHANGE_PASSWORD = "change_password";

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, AccountActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.account_fragment_container, AccountFragment.newInstance())
                    .commit();
        }
    }

    public void onFragmentSelected(@Nonnull String fragmentTag) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.account_fragment_container, getFragmentForTag(fragmentTag))
                .commit();
    }

    private Fragment getFragmentForTag(@Nonnull String fragmentTag) {
        switch (fragmentTag) {
            case FRAGMENT_CHANGE_EMAIL:
                return ChangeEmailFragment.newInstance();
            case FRAGMENT_CHANGE_PASSWORD:
                return ChangePasswordFragment.newInstance();
            default:
                throw new RuntimeException("Unknown fragment type");
        }
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
