package com.shoutit.app.android.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.shoutit.app.android.api.model.ApiMessageResponse;

import rx.functions.Action1;

public class ApiMessagesHelper {

    @NonNull
    public static Action1<ApiMessageResponse> apiMessageAction(@NonNull final Activity activity) {
        return apiMessageResponse -> showApiMessage(activity, apiMessageResponse);
    }

    public static void showApiMessage(@NonNull Activity activity, @NonNull ApiMessageResponse apiMessageResponse) {
        ColoredSnackBar.success(ColoredSnackBar.contentView(activity), apiMessageResponse.getSuccess(), Snackbar.LENGTH_SHORT)
                .show();
    }

    public static void showApiMessageToast(@NonNull Context context, @NonNull ApiMessageResponse apiMessageResponse) {
        Toast.makeText(context, apiMessageResponse.getSuccess(), Toast.LENGTH_SHORT).show();
    }
}
