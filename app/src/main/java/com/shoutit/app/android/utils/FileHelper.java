package com.shoutit.app.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.appunite.rx.ResponseOrError;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.shoutit.app.android.dagger.ForActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class FileHelper {

    private final Context context;

    @Inject
    public FileHelper(@ForActivity Context context) {
        this.context = context;
    }

    @Nonnull
    public Observable<ResponseOrError<File>> saveBitmapToTempFileObservable(@Nonnull final Bitmap bitmap) {
        return Observable.create(new Observable.OnSubscribe<ResponseOrError<File>>() {
            @Override
            public void call(Subscriber<? super ResponseOrError<File>> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    try {
                        subscriber.onNext(ResponseOrError.fromData(saveBitmapToTempFile(bitmap)));
                    } catch (Exception e) {
                        subscriber.onNext(ResponseOrError.<File>fromError(e));
                    } finally {
                        subscriber.onCompleted();
                    }
                }
            }
        });
    }

    @Nonnull
    private File saveBitmapToTempFile(@Nonnull Bitmap bitmap) throws Exception {
        final File tmpFile = new File(context.getExternalCacheDir(), String.format("tmpFile-%s", bitmap.toString()));

        OutputStream os = null;
        try {
            os = new FileOutputStream(tmpFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, os);
            os.flush();
            return tmpFile;
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            throw e;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
                    throw e;
                }
            }
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
                ByteStreams.copy(inputStream, output);
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
