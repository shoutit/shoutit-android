package com.shoutit.app.android.view.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraUtils {

    public static Bitmap getScaledBitmapFromFile(String path, int maxSize) {
        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);

        final int inWidth = opts.outWidth;
        final int inHeight = opts.outHeight;

        final BitmapFactory.Options newOpts = new BitmapFactory.Options();
        if (Math.max(inHeight, inWidth) > maxSize) {
            newOpts.inSampleSize = (int) Math.ceil(Math.max(inWidth / maxSize, inHeight / maxSize));
        }
        return BitmapFactory.decodeFile(path, newOpts);
    }

    @SuppressWarnings("unused")
    public static boolean bitmapToJpeg(Bitmap source, File target, int quality) {
        return bitmapToFile(source, target, Bitmap.CompressFormat.JPEG, quality);
    }

    public static boolean bitmapToFile(Bitmap source, File target, Bitmap.CompressFormat format, int quality) {
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(target);
            source.compress(format, quality, out);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                Log.e("size after compression", "" + target.length());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static final double ASPECT_TOLERANCE = 0.1;

    @SuppressWarnings("unused")
    public static Camera.Size getOptimalPictureSize(int displayOrientation,
                                                    int width,
                                                    int height,
                                                    Camera.Parameters parameters) {
        double targetRatio;

        if (displayOrientation == 90 || displayOrientation == 270) {
            targetRatio = (double) height / width;
        } else {
            targetRatio = (double) width / height;
        }

        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;

            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }

        return optimalSize;
    }
}