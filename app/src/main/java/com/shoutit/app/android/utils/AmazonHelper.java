package com.shoutit.app.android.utils;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.appunite.rx.ResponseOrError;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.constants.AmazonConstants;

import java.io.File;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class AmazonHelper {

    public static final String JPEG = ".jpg";

    @Nonnull
    private final TransferUtility transferUtility;
    @Nonnull
    private final UserPreferences userPreferences;
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

        AmazonBucket(String bucketName, String bucketUrl) {
            this.bucketName = bucketName;
            this.bucketUrl = bucketUrl;
        }
    }

    @Inject
    public AmazonHelper(@Nonnull TransferUtility transferUtility, @Nonnull UserPreferences userPreferences) {
        this.transferUtility = transferUtility;
        this.userPreferences = userPreferences;
        userId = userPreferences.getUser().getId();
    }

    public Observable<ResponseOrError<String>> uploadImageObservable(@Nonnull final AmazonBucket bucket,
                                                                     @Nonnull final File fileToUpload) {
        final String fileName = getImageFileName();

        return Observable.create(new Observable.OnSubscribe<ResponseOrError<String>>() {
            @Override
            public void call(final Subscriber<? super ResponseOrError<String>> subscriber) {
                final TransferObserver upload = transferUtility
                        .upload(bucket.bucketName, fileName, fileToUpload);

                upload.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED && !subscriber.isUnsubscribed()) {
                            subscriber.onNext(ResponseOrError.fromData(
                                    String.format("%1$s/%2$s", bucket.bucketUrl, fileName))
                            );
                            subscriber.onCompleted();
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(ResponseOrError.<String>fromError(ex));
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }

    @Nonnull
    private String getImageFileName() {
        return String.format("%1$d_%2$s%3$s", System.currentTimeMillis(), userId, JPEG);
    }
}
