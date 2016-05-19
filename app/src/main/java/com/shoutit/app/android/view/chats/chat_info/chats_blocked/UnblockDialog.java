package com.shoutit.app.android.view.chats.chat_info.chats_blocked;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.shoutit.app.android.dagger.ForActivity;

import javax.inject.Inject;

public class UnblockDialog {

    private final Context mContext;

    @Inject
    public UnblockDialog(@ForActivity Context context) {
        mContext = context;
    }

    public void show(final String id, String name, final ChatBlockedUsersPresenter presenter) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.unblockUser(id);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setMessage(String.format("Do you want to unblock user %1$s?", name))
                .create();
        alertDialog.show();
    }

}
