package com.shoutit.app.android.view.media;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;

import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MediaUtils {

    public static int getVideoLength(Context context, String path) {
        return getVideoLength(context, new File(path));
    }

    public static int getVideoLength(Context context, File video) {
        return getVideoLength(context, Uri.parse(video.getAbsolutePath()));
    }

    public static int getVideoLength(Context context, Uri videoContentUri) {
        MediaPlayer mp = MediaPlayer.create(context, videoContentUri);
        int duration = mp.getDuration();
        mp.release();

        return duration;
    }

    public static File getMediaDirectory(Context context, String type, boolean externalStorage) {
        if (externalStorage) {
            return Environment.getExternalStoragePublicDirectory(type);
        } else {
            return context.getExternalFilesDir(type);
        }
    }

    public static File createVideoThumbnail(Context context, Uri videoUri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, videoUri);

        Bitmap thumbnail = retriever.getFrameAtTime(1000);
        File outputDir = context.getCacheDir();

        File thumbnailFile = File.createTempFile(videoUri.getLastPathSegment() + "_thumbnail", null, outputDir);
        FileOutputStream os = new FileOutputStream(thumbnailFile);
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, os);

        return thumbnailFile;
    }
}
