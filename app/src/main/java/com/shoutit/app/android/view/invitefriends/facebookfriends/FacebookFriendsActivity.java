package com.shoutit.app.android.view.invitefriends.facebookfriends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.shoutit.app.android.view.profile.ProfileIntentHelper;
import com.shoutit.app.android.view.profile.user.UserProfileActivity;
import com.shoutit.app.android.view.profileslist.BaseProfilesListActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class FacebookFriendsActivity extends BaseProfilesListActivity {

    @Inject
    CallbackManager callbackManager;

    private FacebookFriendsPresenter presenter;

    public static Intent newIntent(Context context) {
        return new Intent(context, FacebookFriendsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        presenter = (FacebookFriendsPresenter) ((FacebookFriendsActivityComponent)
                getActivityComponent()).profilesListPresenter();

        presenter.getProfileSelectedObservable()
                .compose(this.<BaseProfile>bindToLifecycle())
                .subscribe(baseProfile -> {
                    startActivityForResult(
                            ProfileIntentHelper.newIntent(FacebookFriendsActivity.this, baseProfile),
                            REQUEST_OPENED_PROFILE_WAS_LISTENED);
                });

        presenter.getPermissionsNotGrantedObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(ignore -> {
                    Toast.makeText(this, R.string.facebook_friends_permission_error, Toast.LENGTH_LONG).show();
                    finish();
                });

        presenter.getInvitationCodeObservable()
                .compose(bindToLifecycle())
                .subscribe(invitationCode -> {
                    FacebookHelper.showAppInviteDialog(this,
                            FacebookHelper.FACEBOOK_SHARE_APP_LINK,
                            callbackManager,
                            invitationCode);
                });
    }

    protected void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.facebook_friends_ab_title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_facebook_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.facebook_friends_menu_refresh:
                presenter.refreshData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
