package com.shoutit.app.android.utils;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.common.base.Preconditions;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.constants.AmazonConstants;

import java.io.File;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class AmazonHelper {

    public static final String JPEG = ".jpg";
    public static final String MP4 = ".mp4";

    @Nonnull
    private final TransferUtility transferUtility;
    private final String userId;

    public enum AmazonBucket {
        SHOUT(AmazonConstants.BUCKET_SHOUT_NAME,
                AmazonConstants.BUCKET_SHOUT_URL),
        USER(AmazonConstants.BUCKET_USER_NAME,
                AmazonConstants.BUCKET_USER_URL),
        TAG(AmazonConstants.BUCKET_TAG_NAME,
                AmazonConstants.BUCKET_TAG_URL);

        public final String bucketName;
        public final String bucketUrl;

        AmazonBucket(@NonNull String bucketName, @NonNull String bucketUrl) {
            this.bucketName = bucketName;
            this.bucketUrl = bucketUrl;
        }
    }

    @Inject
    public AmazonHelper(@Nonnull TransferUtility transferUtility, @Nonnull UserPreferences userPreferences) {
        this.transferUtility = transferUtility;
        Preconditions.checkNotNull(userPreferences.getUser());
        userId = userPreferences.getUser().getId();
    }

    public Observable<String> uploadShoutMediaVideoObservable(@Nonnull final File fileToUpload) {
        return uploadImageObservable(AmazonBucket.SHOUT, fileToUpload, getVideoFileName());
    }

    public Observable<String> uploadShoutMediaImageObservable(@Nonnull final File fileToUpload) {
        return uploadImageObservable(AmazonBucket.SHOUT, fileToUpload, getImageFileName());
    }

    public Observable<String> uploadUserImageObservable(@Nonnull final File fileToUpload) {
        return uploadImageObservable(AmazonBucket.USER, fileToUpload, getImageFileName());
    }

    private Observable<String> uploadImageObservable(@Nonnull final AmazonBucket bucket,
                                                     @Nonnull final File fileToUpload, final String fileName) {
        return Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(final Subscriber<? super String> subscriber) {
                        final TransferObserver upload = transferUtility
                                .upload(bucket.bucketName, fileName, fileToUpload);

                        upload.setTransferListener(new TransferListener() {
                            @Override
                            public void onStateChanged(int id, TransferState state) {
                                if (state == TransferState.COMPLETED && !subscriber.isUnsubscribed()) {
                                    subscriber.onNext(String.format("%1$s/%2$s", bucket.bucketUrl, fileName));
                                    subscriber.onCompleted();
                                }
                            }

                            @Override
                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onError(ex);
                                }
                            }
                        });
                    }
                });
    }

    @SuppressLint("DefaultLocale")
    @Nonnull
    private String getImageFileName() {
        return String.format("%1$d_%2$s%3$s", System.currentTimeMillis(), userId, JPEG);
    }

    @SuppressLint("DefaultLocale")
    @Nonnull
    private String getVideoFileName() {
        return String.format("%1$d_%2$s%3$s", System.currentTimeMillis(), userId, MP4);
    }

    public static File getfileFromPath(@NonNull String path) {
        return new File(path.replace("file://", ""));
    }
}
