package com.shoutit.app.android.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.google.common.base.Strings;
import com.shoutit.app.android.R;

import rx.functions.Action1;

public class ReportDialog {

    public static void show(final Context context, final Action1<String> callback) {
        final EditText editText = new EditText(context);
        editText.setHint(R.string.report_dialog_hint);

        final int spacing = context.getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        new AlertDialog.Builder(context)
                .setTitle(R.string.shout_bottom_bar_report)
                .setView(editText, spacing, spacing / 2, spacing, spacing / 2)
                .setPositiveButton(context.getString(R.string.send_report_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String reportBody = editText.getText().toString();
                        if (Strings.isNullOrEmpty(reportBody)) {
                            editText.setError(context.getString(R.string.report_dialog_empty_error));
                            dialog.dismiss();
                            return;
                        }

                        callback.call(reportBody);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel_button), null)
                .show();
    }

}
