package com.shoutit.app.android.view.invitefriends.contactsfriends;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;
import com.shoutit.app.android.view.profileslist.BaseProfilesListActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContactsFriendsActivity extends BaseProfilesListActivity {

    private static final int REQUEST_CODE_CONTACTS_PERMISSION = 1;
    private ContactsFriendsPresenter presenter;

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, ContactsFriendsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = (ContactsFriendsPresenter) ((ContactsFriendsActivityComponent)
                getActivityComponent()).profilesListPresenter();

        presenter.getProfileSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(userName -> {
                    startActivityForResult(
                            UserOrPageProfileActivity.newIntent(ContactsFriendsActivity.this, userName),
                            REQUEST_OPENED_PROFILE_WAS_LISTENED);
                });

        presenter.getSuccessFetchContacts()
                .compose(bindToLifecycle())
                .subscribe(responseBodyResponseOrError -> {
                    presenter.refreshData(); 
                });

        if (PermissionHelper.checkPermissions(this,
                REQUEST_CODE_CONTACTS_PERMISSION,
                ColoredSnackBar.contentView(this),
                R.string.permission_contacts_explanation,
                new String[] {Manifest.permission.READ_CONTACTS})) {

            presenter.fetchContacts();
        }
    }

    @Override
    protected void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.contacts_friends_ab_title);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_CONTACTS_PERMISSION) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                presenter.fetchContacts();
            }
            PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ContactsFriendsActivityComponent component = DaggerContactsFriendsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .contactsFriendsActivityModule(new ContactsFriendsActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
