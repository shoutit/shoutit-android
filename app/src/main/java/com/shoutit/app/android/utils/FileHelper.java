package com.shoutit.app.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.appunite.rx.ResponseOrError;
import com.google.common.io.ByteStreams;
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
import rx.functions.Func1;

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
    public File saveBitmapToTempFile(@Nonnull Bitmap bitmap) throws Exception {
        return saveBitmapToTempFile(context, bitmap);
    }

    @Nonnull
    public static File saveBitmapToTempFile(Context context, @Nonnull Bitmap bitmap) throws Exception {
        final File tmpFile = new File(context.getExternalCacheDir(), String.format("tmpFile-%s", bitmap.toString()));

        OutputStream os = null;
        try {
            os = new FileOutputStream(tmpFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, os);
            os.flush();
            return tmpFile;
        } catch (Exception e) {
            Log.e("FileHelper", "Error writing bitmap", e);
            throw e;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e("FileHelper", "Error writing bitmap", e);
                }
            }
        }
    }

    @Nonnull
    public String createTempFileAndStoreUri(@Nonnull Uri fileUri) throws IOException {
        return createTempFileAndStoreUri(context, fileUri);
    }

    @Nonnull
    public static String createTempFileAndStoreUri(Context context, @Nonnull Uri fileUri) throws IOException {
        final File tempFile = createTempFile(context, fileUri, fileUri.getLastPathSegment());
        final Uri tempUri = Uri.fromFile(tempFile);
        return tempUri.getPath();
    }

    public File createTempFile(Uri uri, String id) throws WrongFileException {
        return createTempFile(context, uri, id);
    }

    public static File createTempFile(Context context, Uri uri, String id) throws WrongFileException {
        if (uri == null) {
            return null;
        }
        final File tmpFile = new File(context.getExternalCacheDir(), String.format("tmpFile-%s", id));
        copyUriToFile(context, uri, tmpFile);
        return tmpFile;
    }

    public File copyUriToFile(Uri uri, File destFile) throws WrongFileException {
        return copyUriToFile(context, uri, destFile);
    }

    public static File copyUriToFile(Context context, Uri uri, File destFile) throws WrongFileException {
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

    @NonNull
    public Func1<Uri, Observable<ResponseOrError<File>>> scaleAndCompressImage(final int maxImageSize) {
        return imageUri -> {
            try {
                return scaleAndCompressImage(maxImageSize, imageUri);
            } catch (IOException e) {
                return Observable.just(ResponseOrError.<File>fromError(e));
            }
        };
    }

    @NonNull
    public Observable<ResponseOrError<File>> scaleAndCompressImage(final int maxImageSize, Uri imageUri) throws IOException {
        final String tempFile = createTempFileAndStoreUri(imageUri);
        final Bitmap bitmapToUpload = ImageHelper.scaleImage(tempFile, maxImageSize);
        return saveBitmapToTempFileObservable(bitmapToUpload);
    }
}
