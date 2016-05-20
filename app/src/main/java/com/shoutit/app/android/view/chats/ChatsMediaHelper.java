package com.shoutit.app.android.view.chats;

import android.content.Context;
import android.net.Uri;

import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.view.media.MediaUtils;

import java.io.File;

import rx.Observable;
import rx.Scheduler;

public class ChatsMediaHelper {

    public static Observable<String> uploadChatImage(AmazonHelper amazonHelper, Uri url, Context context, Scheduler ioScheduler, Scheduler uiScheduler) {
        if (url != null) {
            try {
                final File fileFromUri = MediaUtils.createFileFromUri(context, url, MediaUtils.MAX_MEDIA_SIZE);
                return amazonHelper.uploadGroupChatObservable(fileFromUri)
                        .subscribeOn(ioScheduler)
                        .observeOn(uiScheduler);
            } catch (Exception e) {
                return Observable.error(e);
            }
        } else {
            return Observable.just(null);
        }
    }

}
