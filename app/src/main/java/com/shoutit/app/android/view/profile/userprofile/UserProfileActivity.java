package com.shoutit.app.android.view.profile.userprofile;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.profile.ProfileActivity;
import com.shoutit.app.android.view.profile.ProfileActivityComponent;
import com.shoutit.app.android.view.profile.myprofile.MyProfilePresenter;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class UserProfileActivity extends ProfileActivity {

    private PopupMenu popupMenu;

    @Bind(R.id.profile_popup_menu_anchor_view)
    View popupAnchorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setUpPopupMenu();

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
                        popupMenu.show();
                    }
                });
    }

    private void setUpPopupMenu() {
        popupMenu = new PopupMenu(this, popupAnchorView);
        popupMenu.inflate(R.menu.menu_profile_popup);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(UserProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }


}
