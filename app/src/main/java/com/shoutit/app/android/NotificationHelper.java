package com.shoutit.app.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.view.main.MainActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    private final Picasso picasso;

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

    public void sendNotification(@NonNull Bundle bundle, @NonNull Context context) {

        final String title = bundle.getString(GCM_TITLE_FIELD) != null ?
                bundle.getString(GCM_TITLE_FIELD) : context.getString(R.string.app_name);
        final String body = bundle.getString(GCM_BODY_FIELD);
        final String iconUrl = bundle.getString(GCM_ICON_FIELD);

        final JSONObject dataObject;
        String appUrl;
        try {
            dataObject = new JSONObject(bundle.getString(GCM_DATA_FIELD));
            appUrl = dataObject.getString(GCM_APP_URL_FIELD);
        } catch (JSONException e) {
            appUrl = null;
        }

        Bitmap largeIcon = null;
        try {
            largeIcon = getLargeIcon(iconUrl);
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
                .notify(0, notificationBuilder.build());
    }

    @Nullable
    public Bitmap getLargeIcon(@Nullable String imageUrl) throws IOException {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }

        return picasso.load(imageUrl)
                .resizeDimen(R.dimen.notification_large_icon, R.dimen.notification_large_icon)
                .centerCrop()
                .get();
    }
}
