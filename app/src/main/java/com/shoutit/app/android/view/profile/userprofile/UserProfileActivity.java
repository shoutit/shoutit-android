package com.shoutit.app.android.view.profile.userprofile;

import android.os.Bundle;
import android.widget.Toast;

import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.profile.ProfileActivity;
import com.shoutit.app.android.view.profile.ProfileActivityComponent;
import com.shoutit.app.android.view.profile.myprofile.MyProfilePresenter;

import rx.functions.Action1;

public class UserProfileActivity extends ProfileActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final UserProfilePresenter userProfilePresenter = (UserProfilePresenter)
                ((ProfileActivityComponent) getActivityComponent()).getPresenter();

        userProfilePresenter.getOnChatIconClickedSubject()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        Toast.makeText(UserProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });

        userProfilePresenter.getMoreMenuOptionClickedSubject()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        Toast.makeText(UserProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
