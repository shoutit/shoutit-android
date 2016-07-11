package com.shoutit.app.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

public class ImageHelper {
    public static final int DEFAULT_MAX_IMAGE_SIZE = 1024 * 768;
    public static final int MAX_AVATAR_SIZE = 720 * 720;
    public static final int MAX_COVER_SIZE = 1024 * 768;

    public static void setStartCompoundRelativeDrawable(@Nonnull TextView textView, @DrawableRes int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableId, 0, 0, 0);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
        }
    }

    public static void setStartCompoundRelativeDrawable(@Nonnull TextView textView, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
    }

    @Nullable
    public static Bitmap scaleImage(@NonNull String filePath, int maxImageSize) {
        final Bitmap bitmap = downSampleIfNeeded(filePath, maxImageSize);
        if (bitmap == null) {
            return null;
        }

        return adjustImageOrientation(bitmap, filePath);
    }

    @Nullable
    private static Bitmap downSampleIfNeeded(@NonNull String filePath, int maxImageSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, maxImageSize);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    // Fix for some Samsung devices
    @Nullable
    private static Bitmap adjustImageOrientation(@NonNull Bitmap sourceBitmap, String filePath) {
        try {
            if (filePath == null) {
                return null;
            }

            final ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            Log.v("ExifInteface .........", "rotation =" + orientation);

            final Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                default:
                    return sourceBitmap;
            }

            return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(),
                    sourceBitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int maxImageSize) {
        int height = options.outHeight;
        int width = options.outWidth;
        int size = height * width;
        int inSampleSize = 1;

        while (size > maxImageSize) {
            inSampleSize *= 2;
            width /= inSampleSize;
            height /= inSampleSize;
            size = height * width;
        }

        return inSampleSize;
    }
}
