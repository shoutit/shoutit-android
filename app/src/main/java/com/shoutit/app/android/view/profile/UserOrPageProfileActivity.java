package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.shoutit.app.android.App;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.view.editprofile.EditProfileActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class UserOrPageProfileActivity extends ProfileActivity {

    public static Intent newIntent(@Nonnull Context context, @Nonnull String userName) {
        return new Intent(context, UserOrPageProfileActivity.class)
                .putExtra(KEY_PROFILE_ID, userName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserOrPageProfilePresenter presenter = (UserOrPageProfilePresenter) ((ProfileActivityComponent) getActivityComponent()).getPresenter();

        presenter.getWebUrlClickedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String webUrl) {
                        startActivity(IntentHelper.websiteIntent(webUrl));
                    }
                });

        presenter.getProfileToOpenObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String userName) {
                        startActivityForResult(
                                UserOrPageProfileActivity.newIntent(UserOrPageProfileActivity.this, userName),
                                REQUEST_PROFILE_OPENED_FROM_PROFILE);
                    }
                });

        // User Profile specific subscriptions
        presenter.getUserProfilePresenter().getOnChatIconClickedSubject()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        Toast.makeText(UserOrPageProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });

        // My Profile specific subscriptions
        presenter.getMyProfilePresenter().getEditProfileClickObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        startActivityForResult(
                                EditProfileActivity.newIntent(UserOrPageProfileActivity.this),
                                REQUEST_CODE_FROM_EDIT_PROFILE);
                    }
                });

        presenter.getMyProfilePresenter().getNotificationsClickObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        Toast.makeText(UserOrPageProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        final String userName = checkNotNull(intent.getStringExtra(KEY_PROFILE_ID));

        final ProfileActivityComponent component = DaggerProfileActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .profileActivityModule(new ProfileActivityModule(userName))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
