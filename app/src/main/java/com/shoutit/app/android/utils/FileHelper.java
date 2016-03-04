package com.shoutit.app.android.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Optional;
import com.shoutit.app.android.dagger.ForActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public class FileHelper {

    private final ContentResolver mContentResolver;
    private final Context context;

    @Inject
    public FileHelper(ContentResolver mContentResolver, @ForActivity Context context) {
        this.mContentResolver = mContentResolver;
        this.context = context;
    }

    public Optional<File> getLocalFile(Uri uri) {
        checkNotNull(uri);

        if ("file".equals(uri.getScheme())) {
            return Optional.of(new File(uri.getPath()));

        } else if ("content".equals(uri.getScheme())) {
            return getFileFromContentResolver(uri);
        }

        return Optional.absent();
    }

    public Optional<File> getFileFromContentResolver(Uri uri) {
        final String[] projection = {MediaStore.MediaColumns.DATA};
        final Cursor cursor = mContentResolver.query(uri, projection, null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            return Optional.absent();
        }
        cursor.moveToFirst();
        final String filePath = cursor.getString(0);
        if (filePath != null) {
            return Optional.of(new File(filePath));
        } else {
            return Optional.absent();
        }
    }

    @Nullable
    public File saveBitmapToTempFile(@Nonnull Bitmap bitmap, @Nonnull String name) {
        final File tmpFile = new File(context.getExternalCacheDir(), String.format("tmpFile-%s", name));

        OutputStream os;
        try {
            os = new FileOutputStream(tmpFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, os);
            os.flush();
            os.close();
            return tmpFile;
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            return null;
        }
    }

    @Nonnull
    public String createTempFileAndStoreUri(@Nonnull Uri fileUri) throws IOException {
        final File tempFile = createTempFile(fileUri, fileUri.getLastPathSegment());
        final Uri tempUri = Uri.fromFile(tempFile);
        return tempUri.getPath();
    }

    public File createTempFile(Uri uri, String id) throws WrongFileException {
        if (uri == null) {
            return null;
        }
        final File tmpFile = new File(context.getExternalCacheDir(), String.format("tmpFile-%s", id));
        copyUriToFile(uri, tmpFile);
        return tmpFile;
    }

    public File copyUriToFile(Uri uri, File destFile) throws WrongFileException {
        if (uri == null) {
            return null;
        }
        try {
            final InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new FileNotFoundException("Could not open file: " + uri.toString());
            }
            try {
                final File cacheFile = new File(context.getCacheDir(), "tmpFile");
                final FileOutputStream output = new FileOutputStream(destFile);
                try {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        output.write(buffer, 0, length);
                    }
                } finally {
                    output.close();
                }
                return cacheFile;
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new WrongFileException(e, uri);
        }
    }

    public static class WrongFileException extends IOException {
        public WrongFileException(Throwable e, Uri uri) {
            super("Could not load photo: " + uri + " file", e);
        }
    }
}
