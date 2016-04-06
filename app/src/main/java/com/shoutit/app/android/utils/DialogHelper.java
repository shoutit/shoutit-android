package com.shoutit.app.android.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public class DialogHelper {

    public static AlertDialog createInviteDialog(String caller, DialogInterface.OnClickListener acceptClickListener, DialogInterface.OnClickListener rejectClickListener, Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Incoming Call");
        alertDialogBuilder.setMessage(caller + " is calling");
        alertDialogBuilder.setPositiveButton("Accept", acceptClickListener);
        alertDialogBuilder.setNegativeButton("Reject", rejectClickListener);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

}