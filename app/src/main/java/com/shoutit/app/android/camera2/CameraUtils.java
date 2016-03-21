package com.shoutit.app.android.camera2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraUtils {

    public static Bitmap getResizedImage(File imageFile, int maxSize) throws IOException {
        FileInputStream fis = new FileInputStream(imageFile);
        Bitmap imageBitmap = BitmapFactory.decodeStream(fis);
        fis.close();

        return getResizedBitmap(imageBitmap, maxSize);
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
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