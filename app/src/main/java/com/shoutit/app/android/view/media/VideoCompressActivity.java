package com.shoutit.app.android.view.media;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.util.StringUtils;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.R;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoCompressActivity extends Activity {

    private static final String EXTRA_LOCAL_MEDIA = "EXTRA_LOCAL_MEDIA";
    public static final String EXTRA_COMPRESSED_MEDIA = "EXTRA_COMPRESSED_MEDIA";

    private Media media;
    private String oldCopyPath;
    private final String TAG = VideoCompressActivity.class.getName();
    private static final Pattern FFMPEG_RESPONSE_PATTERN = Pattern.compile("time=(\\S*)\\s");

    public static Intent newIntent(Media image, final Context context) {
        final Intent toCompressActivity = new Intent(context, VideoCompressActivity.class);
        toCompressActivity.putExtra(EXTRA_LOCAL_MEDIA, image);
        return toCompressActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_video_compress);
        media = getIntent().getParcelableExtra(EXTRA_LOCAL_MEDIA);

        final File fileVideo = new File(media.getPath());

        //First Copy file to our directory
        oldCopyPath = copyFileToFolderDiffname(fileVideo.getAbsolutePath(), VideoUtils.getWorkingFolder() + File.separator);

        if (BuildConfig.DEBUG) {
            Log.i("oldPath", oldCopyPath);
        }
        final String newPath = VideoUtils.getOutputPath(fileVideo.getName());
        final String[] complexCommand = {
                "-i", oldCopyPath,
                "-filter_complex", "scale=-2:360,format=yuv420p",
                "-c:v", "libopenh264",
                "-b:v", "1000k",
                "-c:a", "aac",
                "-strict", "-2",
                "-y", newPath};
        final String command = StringUtils.join(" ", complexCommand);
        if (BuildConfig.DEBUG) {
            Log.i("command is ", command);
        }
        doTheWorking(command, newPath, media.getDuration());
    }


    public String copyFileToFolderDiffname(String filePath, String folderPath) {
        Log.i(TAG, "Coping file: " + filePath + " to: " + folderPath);
        try {
            final String path = folderPath + VideoUtils.getValidFileNameFromPathWithtimStamp(filePath);
            Files.copy(new File(filePath), new File(path));
            return path;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return filePath;
        }
    }

    private void doTheWorking(String command, final String newPath, final long duration) {
        final ProgressDialog progressBar = getProgressBar();
        final FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    progressBar.show();
                }

                @Override
                public void onProgress(String message) {
                    Log.i("onProgress", message);
                    final Matcher matcher = FFMPEG_RESPONSE_PATTERN.matcher(message);
                    if (matcher.find()) {
                        final String match = matcher.group(1);
                        if (!TextUtils.isEmpty(match)) {
                            long time = 0;
                            try {
                                time = getMillisFromResponse(match);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Couldn't parse number", e);
                            }
                            progressBar.setProgress((int) (time * 100 / duration));
                        }
                    }
                }

                @Override
                public void onFailure(String message) {
                    Log.e("onFailure", message);
                    transcodingFinished(oldCopyPath);
                }

                @Override
                public void onSuccess(String message) {
                    Log.i("onSuccess", message);
                    deleteDuplicates();
                    transcodingFinished(newPath);
                }

                @Override
                public void onFinish() {
                    progressBar.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException ignored) {
        }

    }

    private ProgressDialog getProgressBar() {
        final ProgressDialog progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setTitle("compressing");
        progressBar.setMessage("compressing");
        progressBar.setIndeterminate(false);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        return progressBar;
    }

    public void transcodingFinished(String outputPath) {
        if (BuildConfig.DEBUG) {
            Log.i("paths ", new Gson().toJson(media));
        }

        final Media media = new Media(this.media.getId(), this.media.getName(), outputPath, this.media.getDuration());
        final Intent intent = new Intent().putExtra(EXTRA_COMPRESSED_MEDIA, media);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void deleteDuplicates() {
        final File duplicateFile = new File(oldCopyPath);
        if (duplicateFile.exists()) {
            boolean mDeleted = duplicateFile.delete();
            sendMediaScan(duplicateFile, mDeleted);
        }
    }

    private void sendMediaScan(File duplicateFile, boolean deleted) {
        if (BuildConfig.DEBUG) {
            Log.i("Was the file deleted", String.valueOf(deleted));
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    final Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    final Uri contentUri = Uri.parse("file://" + duplicateFile.getAbsolutePath());
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                } else {
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_MOUNTED,
                            Uri.parse("file://" + duplicateFile.getAbsolutePath())));
                }
            } catch (Exception e) {
                Log.e(TAG, "send media scann", e);
            }
        }
    }

    private long getMillisFromResponse(String match) throws NumberFormatException {
        final String[] tokens = match.split(":");
        final int hours = Integer.parseInt(tokens[0]);
        final int minutes = Integer.parseInt(tokens[1]);
        final int seconds = (int) Float.parseFloat(tokens[2]);
        return (3600 * hours + 60 * minutes + seconds) * 1000;
    }
}
