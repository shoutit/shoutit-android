package com.shoutit.app.android.view.media;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static final String SHOUTIT_PICTURES = "Shoutit Pictures";
    public static final String SHOUTIT_VIDEOS = "Shoutit Videos";

    public static File getPictureDirectory(Context context, boolean externalStorage) {
        File dir = new File(MediaUtils.getMediaDirectory(context, Environment.DIRECTORY_PICTURES, externalStorage), SHOUTIT_PICTURES);
        if (dir.mkdir()) {
            Log.d("tag", "Deleted file : " + dir.getAbsolutePath());
        }
        return dir;
    }

    public static String getPictureName() {
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
        return mFormat.format(new Date()) + ".jpg";
    }

    public static File getVideoDirectory(Context context, boolean externalStorage) {
        File dir = new File(MediaUtils.getMediaDirectory(context, Environment.DIRECTORY_MOVIES, externalStorage), SHOUTIT_VIDEOS);
        if (dir.mkdir()) {
            Log.d("tag", "Deleted file : " + dir.getAbsolutePath());
        }
        return dir;
    }

    public static String getVideoName() {
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
        return mFormat.format(new Date()) + ".mp4";
    }
}
