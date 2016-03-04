package com.shoutit.app.android.utils;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.appunite.rx.ResponseOrError;

import java.io.File;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class AmazonHelper {
    private static final String COVER_INFIX = "_cover_";
    private static final String AVATAR_INFIX = "_avatar_";

    private final TransferUtility transferUtility;

    @Inject
    public AmazonHelper(TransferUtility transferUtility) {
        this.transferUtility = transferUtility;
    }

    public Observable<ResponseOrError<String>> uploadImageObservable(@Nonnull final String bucketUrl,
                                                                     @Nonnull final File fileToUpload) {
        return Observable.create(new Observable.OnSubscribe<ResponseOrError<String>>() {
            @Override
            public void call(final Subscriber<? super ResponseOrError<String>> subscriber) {
                final TransferObserver upload = transferUtility
                        .upload(bucketUrl, fileToUpload.getName(), fileToUpload);

                upload.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED && !subscriber.isUnsubscribed()) {
                            subscriber.onNext(ResponseOrError.fromData(String.format("%1%s/%2$s", bucketUrl, fileToUpload.getName())));
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

    public String getCoverFileName(@Nonnull String username) {
        return username + COVER_INFIX + System.currentTimeMillis();
    }

    public String getAvatarFileName(@Nonnull String username) {
        return username + AVATAR_INFIX + System.currentTimeMillis();
    }
}
