package com.shoutit.app.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class PermissionHelper {

    public static boolean checkPermissions(final Activity activity, final int requestCode,
                                           View snackBarParent, @StringRes int explanationTextId,
                                           @NonNull String[] permissions) {
        boolean hasRequiredPermissions = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                hasRequiredPermissions = false;
                break;
            }
        }

        if (hasRequiredPermissions) {
            return true;
        } else {
            PermissionHelper.requestPermissions(activity, requestCode, snackBarParent,
                    permissions, explanationTextId);
            return false;
        }
    }

    public static boolean arePermissionsGranted(@NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return grantResults.length > 0;
    }

    private static void requestPermissions(final Activity activity, final int requestCode,
                                           View snackBarParent, final @NonNull String[] permissions,
                                           @StringRes int explanationTextId) {
        boolean shouldShowRequestPermissionRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                shouldShowRequestPermissionRationale = true;
                break;
            }
        }

        if (shouldShowRequestPermissionRationale) {
            Snackbar.make(snackBarParent, explanationTextId, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissions(activity, requestCode, permissions);
                        }
                    })
                    .show();
        } else {
            requestPermissions(activity, requestCode, permissions);
        }
    }

    private static void requestPermissions(Activity activity, int requestCode, String[] permissions) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static boolean hasPermission(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
