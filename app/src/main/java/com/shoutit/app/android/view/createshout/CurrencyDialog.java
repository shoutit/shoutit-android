package com.shoutit.app.android.view.createshout;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.shoutit.app.android.R;

public class CurrencyDialog {

    public static void showDialog(@NonNull Context context) {
        new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.edit_currency_info))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}
