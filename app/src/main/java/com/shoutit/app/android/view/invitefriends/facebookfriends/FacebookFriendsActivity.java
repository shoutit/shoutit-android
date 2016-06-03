package com.shoutit.app.android.view.invitefriends.facebookfriends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;

import com.facebook.CallbackManager;
import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;
import com.shoutit.app.android.view.profileslist.BaseProfilesListActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.functions.Action1;

public class FacebookFriendsActivity extends BaseProfilesListActivity {

    @Inject
    CallbackManager callbackManager;

    public static Intent newIntent(Context context) {
        return new Intent(context, FacebookFriendsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final FacebookFriendsPresenter presenter = (FacebookFriendsPresenter) ((FacebookFriendsActivityComponent)
                getActivityComponent()).profilesListPresenter();

        presenter.getProfileToOpenObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String userName) {
                        startActivityForResult(
                                UserOrPageProfileActivity.newIntent(FacebookFriendsActivity.this, userName),
                                REQUEST_OPENED_PROFILE_WAS_LISTENED);
                    }
                });

        presenter.getPermissionsNotGrantedObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean ignore) {
                        ColoredSnackBar.error(ColoredSnackBar.contentView(FacebookFriendsActivity.this),
                                R.string.facebook_friends_permission_error, Snackbar.LENGTH_LONG).show();
                    }
                });

        presenter.getActionOnlyForLoggedInUser()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(this),
                        R.string.error_action_only_for_logged_in_user));
    }

    protected void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.facebook_friends_ab_title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final FacebookFriendsActivityComponent component = DaggerFacebookFriendsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .facebookFriendsActivityModule(new FacebookFriendsActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
