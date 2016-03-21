package com.shoutit.app.android.camera2;


import android.app.Fragment;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VideoUtils {
    public static final String EXTRA_LOCAL_MEDIA = "EXTRA_LOCAL_MEDIA";

    public static void CompressVideo(ArrayList<Image> dataSet, final Fragment context) {

        Intent toCompressActivity = new Intent(context.getActivity(), VideoCompressActivity.class);
        toCompressActivity.putParcelableArrayListExtra(EXTRA_LOCAL_MEDIA, dataSet);
        context.startActivityForResult(toCompressActivity, CameraFragment.RC_MEDIA_COMPRESS);
    }

    public static String getValidFileNameFromPathWithtimStamp(String path) {
        int startIndex = path.lastIndexOf("/") + 1;
        int endIndex = path.lastIndexOf(".");

        String name = path.substring(startIndex, endIndex);
        String ext = path.substring(endIndex + 1);
        String validName = (name.replaceAll("\\Q.\\E", "_")).replaceAll(" ", "_");
        return validName + System.currentTimeMillis() + "." + ext;
    }

    public static String getValidFileNameFromPath(String path) {
        int startIndex = path.lastIndexOf("/") + 1;
        int endIndex = path.lastIndexOf(".");

        String name = path.substring(startIndex, endIndex);
        return (name.replaceAll("\\Q.\\E", "_")).replaceAll(" ", "_");
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String getOutputPath(String videFileName) {
        videFileName = getValidFileNameFromPath(getWorkingFolder() + File.separator + videFileName) + ".mp4";
        File file = new File(getWorkingFolder() + File.separator + videFileName);
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String outputPath = file.getAbsolutePath();
        file.setExecutable(true);
        Log.e("output video path", outputPath);
        return outputPath;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String getWorkingFolder() {

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Shout" + File.separator);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder.getAbsolutePath();
    }

    public static long getDuration(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Long.parseLong(time);
    }
}
