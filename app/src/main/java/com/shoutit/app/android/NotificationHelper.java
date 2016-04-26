package com.shoutit.app.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.shoutit.app.android.view.main.MainActivity;

import rx.functions.Action1;

public class NotificationHelper {

    private static final String GCM_MESSAGE_FIELD = "message";
    private static final String GCM_BODY_FIELD = "body";
    private static final String GCM_MESSAGE_TYPE = "type";

    private static final String VIDEO_CALL_TYPE = "video_call";
    private static final String VIDEO_CALL_MISSED_TYPE = "missed_video_call";

    public static Action1<Bundle> sendNotificationAction(@NonNull final Context context) {
        return new Action1<Bundle>() {
            @Override
            public void call(Bundle bundle) {
                sendNotification(bundle, context);
            }
        };
    }

    public static void sendNotification(@NonNull Bundle bundle,
                                        @NonNull Context context) {

        final String message = bundle.getString(GCM_MESSAGE_FIELD);
        final String body = bundle.getString(GCM_BODY_FIELD);
        final String type = bundle.getString(GCM_MESSAGE_TYPE);

        if (VIDEO_CALL_MISSED_TYPE.equals(type)) {
            showNotification(context, message, body);
        } else if (!VIDEO_CALL_TYPE.equals(type)) {
            showNotification(context, context.getString(R.string.app_name), message);
        }
    }

    public static void showNotification(Context context, String title, String contentText) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(0, notificationBuilder.build());
    }
}
