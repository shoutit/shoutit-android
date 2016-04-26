package com.shoutit.app.android.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.PusherMessage;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.model.Stats;
import com.shoutit.app.android.view.chats.PresenceChannelEventListenerAdapter;

import java.io.IOException;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;

public class PusherHelper {

    private static final String NEW_MESSAGE = "new_message";
    private static final String STATS_UPDATE = "stats_update";

    private static final String DEBUG_KEY = "7bee1e468fabb6287fc5";
    private static final String PRODUCTION_KEY = "86d676926d4afda44089";

    private static final String PROFILE_CHANNEL = "presence-v3-p-%1$s";

    private static final String TAG = PusherHelper.class.getCanonicalName();

    private Pusher mPusher;
    private final Gson mGson;
    private final UserPreferences mUserPreferences;
    private final Scheduler uiScheduler;

    public PusherHelper(@NonNull Gson gson,
                        @NonNull UserPreferences userPreferences,
                        @NonNull @UiScheduler Scheduler uiScheduler) {
        mGson = gson;
        mUserPreferences = userPreferences;
        this.uiScheduler = uiScheduler;
    }

    public void init(@NonNull String token) {
        if (mPusher == null) {
            final HttpAuthorizer authorizer = new HttpAuthorizer(BuildConfig.API_URL + "pusher/auth");
            authorizer.setHeaders(ImmutableMap.of("Authorization", "Bearer " + token));
            final PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
            mPusher = new Pusher(PRODUCTION_KEY, options);
        }
    }

    public Pusher getPusher() {
        Preconditions.checkState(mPusher != null);
        return mPusher;
    }

    public static String getProfileChannelName(@NonNull String userId) {
        return String.format(PROFILE_CHANNEL, userId);
    }

    public Observable<PusherMessage> getNewMessageObservable(final String conversationId) {
        return Observable
                .create(new Observable.OnSubscribe<PusherMessage>() {
                    @Override
                    public void call(final Subscriber<? super PusherMessage> subscriber) {
                        getProfileChannel().bind(NEW_MESSAGE, new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                try {
                                    final PusherMessage pusherMessage = mGson.getAdapter(PusherMessage.class).fromJson(data);
                                    if (pusherMessage.getConversationId().equals(conversationId)) {
                                        subscriber.onNext(pusherMessage);
                                    }
                                } catch (IOException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                });
    }

    public Observable<PusherMessage> getNewMessagesObservable() {
        return Observable
                .create(new Observable.OnSubscribe<PusherMessage>() {
                    @Override
                    public void call(final Subscriber<? super PusherMessage> subscriber) {
                        getProfileChannel().bind(NEW_MESSAGE, new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                try {
                                    final PusherMessage pusherMessage = mGson.getAdapter(PusherMessage.class).fromJson(data);
                                    subscriber.onNext(pusherMessage);
                                } catch (IOException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                });
    }

    public Observable<Stats> getStatsObservable() {
        return mUserPreferences.getUserObservable()
                .filter(Functions1.isNotNull())
                .first()
                .flatMap(new Func1<User, Observable<Stats>>() {
                    @Override
                    public Observable<Stats> call(User user) {
                        return Observable
                                .create(new Observable.OnSubscribe<Stats>() {
                                    @Override
                                    public void call(final Subscriber<? super Stats> subscriber) {
                                        getProfileChannel().bind(STATS_UPDATE, new PresenceChannelEventListenerAdapter() {

                                            @Override
                                            public void onEvent(String channelName, String eventName, String data) {
                                                try {
                                                    final Stats pusherStats = mGson.getAdapter(Stats.class).fromJson(data);
                                                    subscriber.onNext(pusherStats);
                                                } catch (IOException e) {
                                                    subscriber.onError(e);
                                                }
                                            }
                                        });
                                    }
                                });
                    }
                })
                .observeOn(uiScheduler);
    }

    public PresenceChannel getProfileChannel() {
        final User user = mUserPreferences.getUser();
        assert user != null;
        return mPusher.getPresenceChannel(PusherHelper.getProfileChannelName(user.getId()));
    }

    public boolean shouldConnect() {
        return mPusher.getConnection().getState() != ConnectionState.CONNECTING && mPusher.getConnection().getState() != ConnectionState.CONNECTED;
    }

    public ConnectionEventListener getEventListener() {
        return new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
                Log.i(TAG, connectionStateChange.getCurrentState().name());
            }

            @Override
            public void onError(String s, String s1, Exception e) {
                Log.e(TAG, "pusher message", e);
            }
        };
    }
}
