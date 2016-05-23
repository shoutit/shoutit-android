package com.shoutit.app.android.view.chats.chat_info.chats_users_list.chats_select;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.inject.Inject;

public class SelectDialog {

    private final Context mContext;

    @Inject
    public SelectDialog(@ForActivity Context context) {
        mContext = context;
    }

    public void show(final String id, String name, final ChatSelectUsersPresenter presenter) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.addProfile(id);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setMessage(String.format(mContext.getString(R.string.chat_select_user_dialog_title), name))
                .create();
        alertDialog.show();
    }

}
