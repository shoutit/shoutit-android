package com.shoutit.app.android.view.profile.myprofile;

import android.os.Bundle;
import android.widget.Toast;

import com.shoutit.app.android.view.profile.ProfileActivity;
import com.shoutit.app.android.view.profile.ProfileActivityComponent;

import rx.functions.Action1;

public class MyProfileActivity extends ProfileActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final MyProfilePresenter myProfilePresenter = (MyProfilePresenter)
                ((ProfileActivityComponent) getActivityComponent()).getPresenter();

        myProfilePresenter.getEditProfileClickObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        Toast.makeText(MyProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });

        myProfilePresenter.getNotificationsClickObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        Toast.makeText(MyProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
