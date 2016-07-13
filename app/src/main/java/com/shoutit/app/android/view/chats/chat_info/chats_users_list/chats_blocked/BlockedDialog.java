package com.shoutit.app.android.view.chats.chat_info.chats_users_list.chats_blocked;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;


public class BlockedDialog {

    private final Context mContext;

    @Bind(R.id.chat_blocked_unblock)
    Button unblockButton;
    @Bind(R.id.chat_blocked_view_profile)
    Button viewProfileButton;

    @Inject
    public BlockedDialog(@ForActivity Context context) {
        mContext = context;
    }

    public void show(String id, String name, @Nonnull String userName, boolean isPage, final ChatBlockedUsersPresenter presenter) {

        final View view = LayoutInflater.from(mContext).inflate(R.layout.chat_blocked_dialog, null, false);

        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(name)
                .setView(view)
                .create();
        ButterKnife.bind(this, view);

        unblockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.showUnblockConfirmDialog(id, name);
                alertDialog.dismiss();
            }
        });

        viewProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.showProfile(userName, isPage);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

}

