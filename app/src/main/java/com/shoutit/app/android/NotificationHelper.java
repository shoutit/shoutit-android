package com.shoutit.app.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.view.main.MainActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.functions.Action1;

public class NotificationHelper {
    private static final String TAG = NotificationHelper.class.getSimpleName();

    private static final String GCM_TITLE_FIELD = "title";
    private static final String GCM_BODY_FIELD = "body";
    private static final String GCM_ICON_FIELD = "icon";
    private static final String GCM_DATA_FIELD = "data";
    private static final String GCM_APP_URL_FIELD = "app_url";
    private static final String GCM_EVENT_NAME = "event_name";

    private static final String EVENT_INCOMING_CALL = "incoming_video_call";
    private static final String EVENT_NEW_MESSAGE = "new_message";
    private static final String EVENT_NEW_NOTIFICATION = "new_notification";

    private static final int DEFAULT_NOTIFICATION_ID = 0;

    private final Picasso picasso;

    private static final Map<String, Integer> notificationIdsMap = new HashMap<>();
    static {
        notificationIdsMap.put(EVENT_INCOMING_CALL, 1);
        notificationIdsMap.put(EVENT_NEW_MESSAGE, 2);
        notificationIdsMap.put(EVENT_NEW_NOTIFICATION, 3);
    }

    @Inject
    public NotificationHelper(Picasso picasso) {
        this.picasso = picasso;
    }

    public Action1<Bundle> sendNotificationAction(@NonNull final Context context) {
        return new Action1<Bundle>() {
            @Override
            public void call(Bundle bundle) {
                sendNotification(bundle, context);
            }
        };
    }

    private void sendNotification(@NonNull Bundle bundle, @NonNull Context context) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Received Push bundle: " + bundle.toString());
        }

        final String eventName = bundle.getString(GCM_EVENT_NAME);
        if (!EVENT_INCOMING_CALL.equals(eventName)) {
            showNotification(bundle, context);
        }
    }

    private void showNotification(Bundle bundle, Context context) {
        final String title = bundle.getString(GCM_TITLE_FIELD) != null ?
                bundle.getString(GCM_TITLE_FIELD) : context.getString(R.string.app_name);
        final String body = bundle.getString(GCM_BODY_FIELD);
        final String iconUrl = bundle.getString(GCM_ICON_FIELD);
        final String eventName = bundle.getString(GCM_EVENT_NAME);

        final JSONObject dataObject;
        String appUrl = null;
        final String dataJson = bundle.getString(GCM_DATA_FIELD);
        if (dataJson != null) {
            try {
                dataObject = new JSONObject(dataJson);
                appUrl = dataObject.getString(GCM_APP_URL_FIELD);
            } catch (JSONException e) {
                LogHelper.logThrowableAndCrashlytics(TAG, "Cannot parse data field from push", e);
                appUrl = null;
            }
        }

        Bitmap largeIcon = null;
        try {
            largeIcon = getLargeIcon(iconUrl, context);
        } catch (IOException e) {
            LogHelper.logThrowableAndCrashlytics(TAG, "Cannot fetch large icon for url: " + iconUrl, e);
        }

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent;
        if (TextUtils.isEmpty(appUrl)) {
            intent = new Intent(context, MainActivity.class);
        } else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl));
        }

        final TaskStackBuilder stackBuilder = TaskStackBuilder
                .create(context)
                .addNextIntent(MainActivity.newIntent(context))
                .addNextIntent(intent);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(largeIcon)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(getNotificationId(eventName), notificationBuilder.build());
    }

    @Nullable
    private Bitmap getLargeIcon(@Nullable String imageUrl,
                                @Nonnull Context context) throws IOException {
        if (TextUtils.isEmpty(imageUrl)) {
            return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        } else {
            return picasso.load(imageUrl)
                    .resizeDimen(R.dimen.notification_large_icon, R.dimen.notification_large_icon)
                    .centerCrop()
                    .get();
        }
    }

    private int getNotificationId(@Nullable String eventName) {
        final Integer id = notificationIdsMap.get(eventName);
        return id != null ? id : DEFAULT_NOTIFICATION_ID;
    }
}
