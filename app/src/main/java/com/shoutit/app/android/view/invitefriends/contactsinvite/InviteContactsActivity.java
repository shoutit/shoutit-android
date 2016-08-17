package com.shoutit.app.android.view.invitefriends.contactsinvite;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.dagger.DaggerBaseDaggerActivityComponent;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Contact;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.utils.PermissionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InviteContactsActivity extends BaseDaggerActivity {

    private static final int REQUEST_CODE_CONTACTS_PERMISSION = 1;

    @Bind(R.id.base_list_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_list_toolbar)
    Toolbar toolbar;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    InviteContactsPresenter presenter;
    @Inject
    InviteContactsAdapter adapter;

    public static Intent newIntent(Context context) {
        return new Intent(context, InviteContactsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_list_layout);
        ButterKnife.bind(this);

        setUpToolbar();

        recyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        presenter.getContactSelectedObservable()
                .compose(this.<Contact>bindToLifecycle())
                .subscribe(contact -> {
                    if (contact.getMobiles().isEmpty() && !contact.getEmails().isEmpty()) {
                        sendEmailInvite(contact.getEmails().get(0));
                    } else if (!contact.getMobiles().isEmpty()) {
                        sendSmsInvite(contact.getMobiles().get(0));
                    }
                });

        presenter.getContactsObservable()
                .compose(bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        if (PermissionHelper.checkPermissions(this,
                REQUEST_CODE_CONTACTS_PERMISSION,
                ColoredSnackBar.contentView(this),
                R.string.permission_contacts_explanation,
                new String[]{Manifest.permission.READ_CONTACTS})) {

            presenter.fetchContacts();
        }
    }

    private void sendSmsInvite(String phoneNumber) {
        startActivity(IntentHelper.getSmsIntent(phoneNumber, getString(R.string.invite_app_invite_text)));
    }

    private void sendEmailInvite(String emailAddress) {
        startActivity(IntentHelper.getEmailIntent(emailAddress, getString(R.string.invite_app_invite_text)));
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.invite_contacts_ab_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
