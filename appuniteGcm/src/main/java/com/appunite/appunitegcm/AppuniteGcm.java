package com.appunite.appunitegcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * Class used to register application to Cloud Messaging and handling push notification.
 */
public class AppuniteGcm {
    private static final String KEY_PUSH_TOKEN_PREFERENCES = "appuniteGcmPushTokenKey";
    public static final String TAG = "AppuniteGCM";

    private static AppuniteGcm instance;
    private final SharedPreferences sharedPreferences;

    /**
     * @param context
     * @param token - get from resources R.string.gcm_defaultSenderId
     * @return
     */
    @NonNull
    public static AppuniteGcm initialize(@NonNull Context context, @NonNull String token) {
        if (instance == null) {
            instance = new AppuniteGcm(context, token);
        }
        return instance;
    }

    /**
     * Obtain instance of AppuniteGcm class
     * @return Instance of a AppuniteGcm
     */
    @NonNull
    public static AppuniteGcm getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GcmPresenter is not initialize");
        }
        return instance;
    }

    @NonNull
    private final BehaviorSubject<String> gcmTokenSubject = BehaviorSubject.create();
    @NonNull
    private final PublishSubject<Bundle> bundlePublishSubject = PublishSubject.create();
    @NonNull
    private final PublishSubject<GcmPushDataWithServerKey> sendPushSubject = PublishSubject.create();

    public AppuniteGcm(final @NonNull Context context,
                       final @NonNull String senderId) {
        final OkHttpClient okHttpClient = new OkHttpClient();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Observable.
                create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        final String pushToken = getPushToken();
                        if (pushToken != null) {
                            subscriber.onNext(pushToken);
                            subscriber.onCompleted();
                            return;
                        }

                        final InstanceID instanceID = InstanceID.getInstance(context);
                        try {
                            final String newPushToken = instanceID.getToken(
                                    senderId,
                                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                            sharedPreferences.edit()
                                    .putString(KEY_PUSH_TOKEN_PREFERENCES, newPushToken)
                                    .apply();
                            subscriber.onNext(newPushToken);
                            subscriber.onCompleted();
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(gcmTokenSubject);

        sendPushSubject
                .flatMap(new Func1<GcmPushDataWithServerKey, Observable<String>>() {
                    @Override
                    public Observable<String> call(final GcmPushDataWithServerKey gcmPushDataWithServerKey) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {
                                final String gcmPushData = gcmPushDataWithServerKey.getGcmPushData();
                                final String gcmServerKey = gcmPushDataWithServerKey.getGcmServerKey();
                                final String pushToken = getPushToken();
                                if (pushToken == null) {
                                    subscriber.onError(new NullPointerException("push token is null"));
                                    return;
                                }
                                final String gcmDate = buildGcmData(pushToken, gcmPushData);
                                final RequestBody body = RequestBody.create(MediaType.parse("application/json"),
                                        gcmDate);
                                final Request request = new Request.Builder()
                                        .addHeader("Content-Type", "application/json")
                                        .addHeader("Authorization", String.format("key=%s", gcmServerKey))
                                        .url("https://android.googleapis.com/gcm/send")
                                        .post(body)
                                        .build();
                                try {
                                    final Response response = okHttpClient.newCall(request).execute();
                                    subscriber.onNext(response.body().string());
                                    subscriber.onCompleted();
                                } catch (IOException e) {
                                    subscriber.onError(e);
                                }
                            }
                        }).subscribeOn(Schedulers.io());

                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        LoggingHelper.log(TAG, s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LoggingHelper.log(TAG, "Error while trying to send push notification", throwable);
                    }
                });
    }

    /**
     * Toggle whether debug logging is enabled.
     * <p>
     * <b>WARNING:</b> Enabling this will result in excessive object allocation. This should be only
     * used for debugging purposes.
     */
    public AppuniteGcm loggingEnabled(boolean enabled) {
        LoggingHelper.setLoggingIsEnabled(enabled);
        return this;
    }

    /**
     * Returns current push token registered in GCM
     */
    @Nullable
    public String getPushToken() {
        return sharedPreferences.getString(KEY_PUSH_TOKEN_PREFERENCES, null);
    }

    /**
     * Preparing a GCM message in JSON form
     * @param pushToken token retrieved after initialization.
     * @param pushData content of the message
     * @return JSON String with the properly formatted message.
     */
    @NonNull
    private String buildGcmData(@NonNull String pushToken, @NonNull String pushData) {
        return String.format("{ \"data\": %s,\n" +
                "  \"to\" : \"%s\"\n" +
                "}\n", pushData, pushToken);
    }

    /**
     * Returns PublishSubject that trigger send action
     */
    @NonNull
    PublishSubject<GcmPushDataWithServerKey> getSendPushObserver() {
        return sendPushSubject;
    }

    /**
     * Returns Observer triggered when notification data arrives
     */
    @NonNull
    Observer<Bundle> getBundlePublishObserver() {
        return bundlePublishSubject;
    }

    /**
     * Returns Observable with Bundle received from GcmListenerService
     */
    @NonNull
    public Observable<Bundle> getPushBundleObservable() {
        return bundlePublishSubject;
    }

    /**
     * Returns Observable for push token registered in GCM
     */
    @NonNull
    public Observable<String> registerGcmTokenObservable() {
        return gcmTokenSubject;
    }

}
