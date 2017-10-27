package com.shoutit.app.android.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.Nullable;

import com.shoutit.app.android.App;

import java.util.List;

import javax.annotation.Nonnull;

public class ProcessUtils {

    public static boolean isInMainProcess(@Nonnull App application) {
        final String processName = getProcessName(application);
        return processName == null || (!processName.endsWith(":cds") && !processName.endsWith(":leakcanary"));
    }

    @Nullable
    private static String getProcessName(@Nonnull Context context) {
        int pid = android.os.Process.myPid();
        final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        if (infos != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : infos) {
                if (processInfo.pid == pid) {
                    return processInfo.processName;
                }
            }
        }
        return null;
    }
}
