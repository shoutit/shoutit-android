package com.shoutit.app.android.camera2;

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
import com.google.gson.Gson;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

public class VideoCompressActivity extends Activity {
    ArrayList<ModelCommands> mCommandCompression;
    private ArrayList<Image> dataSet;
    int iPositionCompressed = 0;
    int mTotalFiles = 0;
    private boolean allFilesFinished;
    private String oldCopyPath;
    private final String TAG = VideoCompressActivity.class.getName();
    private static final Pattern FFMPEG_RESPONSE_PATTERN = Pattern.compile("time=(\\S*)\\s");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_video_compress);
        mCommandCompression = new ArrayList<>();
        dataSet = getIntent().getParcelableArrayListExtra(VideoUtils.EXTRA_LOCAL_MEDIA);

        for (int i = 0; i < dataSet.size(); i++) {
            ModelCommands model = new ModelCommands();
            if (dataSet.get(i).isVideo) {
                File fileVideo = new File(dataSet.get(i).path);
                //First Copy file to our directory
                oldCopyPath = copyFileToFolderDiffname(fileVideo.getAbsolutePath(), VideoUtils.getWorkingFolder() + File.separator);
                if (BuildConfig.DEBUG) {
                    Log.e("oldPath", oldCopyPath);
                }
                String mOutputPath = VideoUtils.getOutputPath(fileVideo.getName());
                String[] complexCommand = {
                        "-i", oldCopyPath,
                        "-filter_complex", "scale=-2:360,format=yuv420p",
                        "-c:v", "libopenh264",
                        "-b:v", "1000k",
                        "-c:a", "aac",
                        "-strict", "-2",
                        "-y", mOutputPath};
                String command = StringUtils.join(" ", complexCommand);
                if (BuildConfig.DEBUG) {
                    Log.i("command is ", command);
                }
                model.mCommand = command;
                model.newPath = mOutputPath;
                model.id = dataSet.get(i).id;
                model.duration = Long.parseLong(dataSet.get(i).duration);
                mCommandCompression.add(model);
            }
        }
        mTotalFiles = mCommandCompression.size();

        doTheWorking();
    }


    public String copyFileToFolderDiffname(String filePath, String folderPath) {
        Log.i(TAG, "Coping file: " + filePath + " to: " + folderPath);
        String validFilePathStr = filePath;
        try {
            FileInputStream is = new FileInputStream(filePath);
            BufferedOutputStream o = null;
            String validFileName = VideoUtils.getValidFileNameFromPathWithtimStamp(filePath);
            validFilePathStr = folderPath + validFileName;
            File destFile = new File(validFilePathStr);
            try {
                byte[] buff = new byte[10000];
                int read;
                o = new BufferedOutputStream(new FileOutputStream(destFile), 10000);
                while ((read = is.read(buff)) > -1) {
                    o.write(buff, 0, read);
                }
            } finally {
                is.close();
                if (o != null) o.close();

            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return validFilePathStr;
    }

    public ProgressDialog progressBar;

    private void doTheWorking() {
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setTitle("compressing");
        progressBar.setMessage("compressing");
        progressBar.setIndeterminate(false);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            String cmd = mCommandCompression.get(iPositionCompressed).mCommand;
            final long duration = mCommandCompression.get(iPositionCompressed).duration;
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    progressBar.show();
                }

                @Override
                public void onProgress(String message) {
                    Log.e("onProgress", message);
                    Matcher matcher = FFMPEG_RESPONSE_PATTERN.matcher(message);
                    if (matcher.find()) {
                        String match = matcher.group(1);
                        if (!TextUtils.isEmpty(match)) {
                            long time = 0;
                            try {
                                time = getMillisFromResponse(match);
                            } catch (NumberFormatException e) {
                                Log.e("dupa", "Couldn't parse number", e);
                            }
                            progressBar.setProgress((int) (time * 100 / duration));
                        }
                    }
                }

                @Override
                public void onFailure(String message) {
                    Log.e("onFailure", message);
                    Toast.makeText(VideoCompressActivity.this, "failed to compress", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onSuccess(String message) {
                    Log.e("onSuccess", message);
                    transcodingFinished();
                }

                @Override
                public void onFinish() {
                    progressBar.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }

    }

    public void transcodingFinished() {
        // change file paths to new compressed paths :)
        for (int i = 0; i < dataSet.size(); i++) {
            if (dataSet.get(i).isVideo) {
                if (dataSet.get(i).id == mCommandCompression.get(iPositionCompressed).id) {
                    dataSet.get(i).path = mCommandCompression.get(iPositionCompressed).newPath;
                }
            }
        }

        //Delete duplicate copied file from our Director

        File fDuplicateFile = new File(oldCopyPath);
        if (fDuplicateFile.exists()) {
            boolean mDeleted = fDuplicateFile.delete();
            if (BuildConfig.DEBUG) {
                Log.e("Was the file deleted", "" + mDeleted);
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Intent mediaScanIntent = new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.parse("file://"
                                + fDuplicateFile.getAbsolutePath());
                        mediaScanIntent.setData(contentUri);
                        sendBroadcast(mediaScanIntent);
                    } else {
                        sendBroadcast(new Intent(
                                Intent.ACTION_MEDIA_MOUNTED,
                                Uri.parse("file://"
                                        + fDuplicateFile.getAbsolutePath())));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // finished with the work
        if ((iPositionCompressed + 1) == mCommandCompression.size()) {
            allFilesFinished = true;
        }

        if (allFilesFinished) {
            // send dataset back
            Intent intent = new Intent();
            if (BuildConfig.DEBUG) {
                Log.e("paths ", new Gson().toJson(dataSet));

            }
            intent.putParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES, dataSet);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            // increase the position
            iPositionCompressed = iPositionCompressed + 1;
            //start the process with new file
            doTheWorking();
        }

    }


    public class ModelCommands {
        public String newPath;
        public String mCommand;
        public long duration;
        public long id;
    }

    private long getMillisFromResponse(String match) throws NumberFormatException {
        String[] tokens = match.split(":");
        int hours = Integer.parseInt(tokens[0]);
        int minutes = Integer.parseInt(tokens[1]);
        int seconds = (int) Float.parseFloat(tokens[2]);
        return  (3600 * hours + 60 * minutes + seconds) * 1000;
    }

}
