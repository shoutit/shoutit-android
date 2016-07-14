package com.shoutit.app.android;

import android.app.Notification;
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

import com.google.common.collect.Maps;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.view.main.MainActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

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
    private static final String GCM_PUSHED_FOR_FIELD = "pushed_for";
    private static final String GCM_EVENT_NAME = "event_name";

    private static final String EVENT_INCOMING_CALL = "incoming_video_call";
    private static final String EVENT_NEW_MESSAGE = "new_message";
    private static final String EVENT_NEW_NOTIFICATION = "new_notification";

    private static final int DEFAULT_NOTIFICATION_ID = 0;

    private final Context mContext;
    private final Picasso picasso;
    private final UserPreferences userPreferences;

    private static final Map<String, Integer> notificationIdsMap = Maps.newHashMap();

    static {
        notificationIdsMap.put(EVENT_INCOMING_CALL, 1);
        notificationIdsMap.put(EVENT_NEW_MESSAGE, 2);
        notificationIdsMap.put(EVENT_NEW_NOTIFICATION, 3);
    }

    @Inject
    public NotificationHelper(@ForApplication Context context, Picasso picasso, UserPreferences userPreferences) {
        mContext = context;
        this.picasso = picasso;
        this.userPreferences = userPreferences;
    }

    @NonNull
    public Action1<Bundle> sendNotificationAction() {
        return this::sendNotification;
    }

    public void sendNotification(@NonNull Bundle bundle) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Received Push bundle: " + bundle.toString());
        }

        final String eventName = bundle.getString(GCM_EVENT_NAME);
        if (!EVENT_INCOMING_CALL.equals(eventName)) {
            showNotificationFromBundle(bundle);
        }
    }

    private void showNotificationFromBundle(Bundle bundle) {
        final String title = bundle.getString(GCM_TITLE_FIELD) != null ?
                bundle.getString(GCM_TITLE_FIELD) : mContext.getString(R.string.app_name);
        final String body = bundle.getString(GCM_BODY_FIELD);
        final String iconUrl = bundle.getString(GCM_ICON_FIELD);
        final String pushedFor = bundle.getString(GCM_PUSHED_FOR_FIELD);
        final String eventName = bundle.getString(GCM_EVENT_NAME);
        final String dataJson = bundle.getString(GCM_DATA_FIELD);

        showNotification(dataJson, title, body, iconUrl, eventName, pushedFor);
    }

    private void showNotification(String dataJson, String title, String body, String iconUrl, String eventName, String pushedFor) {
        final Bitmap largeIcon = getLargeIconOrNull(iconUrl);

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        final PendingIntent pendingIntent = getPendingIntent(dataJson, checkIfNotificationIsForCurrentUser(pushedFor));

        final Notification notification = getNotification(title, body, largeIcon, defaultSoundUri, pendingIntent);

        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(getNotificationId(eventName), notification);
    }

    private boolean checkIfNotificationIsForCurrentUser(String pushedFor) {
        final BaseProfile userOrPage = userPreferences.getUserOrPage();
        assert userOrPage != null;
        final String id = userOrPage.getId();
        return pushedFor.equals(id);
    }

    @NonNull
    private Notification getNotification(String title, String body, Bitmap largeIcon, Uri defaultSoundUri, PendingIntent pendingIntent) {
        return new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(largeIcon)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .build();
    }

    @NonNull
    private PendingIntent getPendingIntent(String dataJson, boolean isForCurrentUser) {
        final TaskStackBuilder stackBuilder = TaskStackBuilder
                .create(mContext)
                .addNextIntent(MainActivity.newIntent(mContext));
        if (isForCurrentUser) {
            final String appUrl = getAppUrl(dataJson);
            final Intent intent = getIntent(appUrl);
            stackBuilder.addNextIntent(intent);
        }

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
    }

    @NonNull
    private Intent getIntent(String appUrl) {
        final Intent intent;
        if (TextUtils.isEmpty(appUrl)) {
            intent = new Intent(mContext, MainActivity.class);
        } else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl));
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Nullable
    private Bitmap getLargeIconOrNull(String iconUrl) {
        try {
            return getLargeIcon(iconUrl);
        } catch (IOException e) {
            LogHelper.logThrowableAndCrashlytics(TAG, "Cannot fetch large icon for url: " + iconUrl, e);
            return null;
        }
    }

    @Nullable
    private String getAppUrl(String dataJson) {
        return getStringFromJson(dataJson, GCM_APP_URL_FIELD);
    }

    @Nullable
    private String getStringFromJson(String dataJson, String field) {
        if (dataJson != null) {
            try {
                final JSONObject dataObject = new JSONObject(dataJson);
                return dataObject.optString(field);
            } catch (JSONException e) {
                LogHelper.logThrowableAndCrashlytics(TAG, "Cannot parse data field from push", e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Nullable
    private Bitmap getLargeIcon(@Nullable String imageUrl) throws IOException {
        if (TextUtils.isEmpty(imageUrl)) {
            return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.notification_large);
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
