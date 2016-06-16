package com.shoutit.app.android.view.createshout;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import com.shoutit.app.android.R;

import rx.functions.Action1;

public class DialogsHelper {

    public static Action1<Object> showDialogAction(@NonNull Context context, @StringRes int message) {
        return o -> showDialog(context, message);
    }

    public static void showDialog(@NonNull Context context, @StringRes int message) {
        new AlertDialog.Builder(context)
                .setMessage(context.getString(message))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void showCurrencyDialog(@NonNull Context context) {
        showDialog(context, R.string.edit_currency_info);
    }

    public static void showOnlyOneVideoDialog(@NonNull Context context) {
        showDialog(context, R.string.edit_only_one_video);
    }

    public static void showShareInfoDialog(@NonNull Context context) {
        showDialog(context, R.string.create_request_share_info);
    }
}
