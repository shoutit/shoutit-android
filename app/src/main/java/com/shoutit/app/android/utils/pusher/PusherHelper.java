package com.shoutit.app.android.utils.pusher;

import android.support.annotation.NonNull;
import android.util.Log;

import com.appunite.rx.dagger.UiScheduler;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.api.Headers;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.NotificationsResponse;
import com.shoutit.app.android.api.model.PusherConversationUpdate;
import com.shoutit.app.android.api.model.PusherMessage;
import com.shoutit.app.android.model.Stats;
import com.shoutit.app.android.utils.BuildTypeUtils;
import com.shoutit.app.android.view.chats.PresenceChannelEventListenerAdapter;

import java.io.IOException;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

public class PusherHelper {

    private static final String EVENT_NEW_MESSAGE = "new_message";
    private static final String EVENT_CONVERSATION_UPDATE = "conversation_update";
    private static final String EVENT_NEW_NOTIFICATION = "new_notification";
    private static final String EVENT_STATS_UPDATE = "stats_update";
    private static final String EVENT_CLIENT_IS_TYPING = "client-is_typing";
    private static final String EVENT_PROFILE_UPDATE = "profile_update";

    private static final String DEBUG_KEY = "7bee1e468fabb6287fc5";
    private static final String LOCAL_KEY = "d6a98f27e49289344791";
    private static final String PRODUCTION_KEY = "86d676926d4afda44089";

    private static final String PROFILE_CHANNEL = "presence-v3-p-%1$s";
    private static final String CONVERSATION_CHANNEL = "presence-v3-c-%1$s";

    private static final String TAG = PusherHelper.class.getCanonicalName();

    private Pusher mPusher;
    private BaseProfile mUser;
    private final Gson mGson;
    private final Scheduler uiScheduler;

    public PusherHelper(@NonNull Gson gson,
                        @NonNull @UiScheduler Scheduler uiScheduler) {
        mGson = gson;
        this.uiScheduler = uiScheduler;
    }

    public void init(@NonNull String token, BaseProfile user) {
        mUser = user;
        if (mPusher == null) {
            final HttpAuthorizer authorizer = new HttpAuthorizer(BuildConfig.API_URL + "pusher/auth");
            if (user.isPage()) {
                authorizer.setHeaders(ImmutableMap.of(Headers.AUTHORIZATION, Headers.TOKEN_PREFIX + token, Headers.AUTHORIZATION_PAGE_ID, user.getId()));
            } else {
                authorizer.setHeaders(ImmutableMap.of(Headers.AUTHORIZATION, Headers.TOKEN_PREFIX + token));
            }
            final PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
            mPusher = new Pusher(getKey(), options);
        }
    }

    private String getKey() {
        if (BuildTypeUtils.isStagingOrDebug()) {
            return DEBUG_KEY;
        } else if (BuildTypeUtils.isLocal()) {
            return LOCAL_KEY;
        } else if (BuildTypeUtils.isRelease()) {
            return PRODUCTION_KEY;
        } else {
            throw BuildTypeUtils.unknownTypeException();
        }
    }

    private static String getProfileChannelName(@NonNull String userId) {
        return String.format(PROFILE_CHANNEL, userId);
    }

    private static String getConversationChannelName(@NonNull String conversationId) {
        return String.format(CONVERSATION_CHANNEL, conversationId);
    }

    public Observable<PusherMessage> getNewMessageObservable(final PresenceChannel conversationChannel) {
        return Observable
                .create(new Observable.OnSubscribe<PusherMessage>() {
                    @Override
                    public void call(final Subscriber<? super PusherMessage> subscriber) {
                        conversationChannel.bind(EVENT_NEW_MESSAGE, new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                try {
                                    final PusherMessage pusherMessage = mGson.getAdapter(PusherMessage.class).fromJson(data);
                                    logMessage(pusherMessage, "conversation channel / getNewMessageObservable");
                                    subscriber.onNext(pusherMessage);
                                } catch (IOException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                });
    }

    public Observable<BaseProfile> getUserUpdatedObservable() {
        return Observable
                .create(new Observable.OnSubscribe<BaseProfile>() {
                    @Override
                    public void call(final Subscriber<? super BaseProfile> subscriber) {
                        getProfileChannel().bind(EVENT_PROFILE_UPDATE, new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                final BaseProfile user = mGson.fromJson(data, BaseProfile.class);
                                subscriber.onNext(user);
                            }
                        });
                    }
                });
    }

    public Observable<PusherConversationUpdate> getConverstionUpdateObservable() {
        return Observable
                .create(new Observable.OnSubscribe<PusherConversationUpdate>() {
                    @Override
                    public void call(final Subscriber<? super PusherConversationUpdate> subscriber) {
                        getProfileChannel().bind(EVENT_CONVERSATION_UPDATE, new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                try {

                                    final PusherConversationUpdate conversationUpdate = mGson.getAdapter(PusherConversationUpdate.class).fromJson(data);
                                    logMessage(data, "profile / getConverstionUpdateObservable");
                                    subscriber.onNext(conversationUpdate);
                                } catch (IOException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                });
    }

    public Observable<NotificationsResponse.Notification> getNewNotificationObservable() {
        return Observable
                .create(new Observable.OnSubscribe<NotificationsResponse.Notification>() {
                    @Override
                    public void call(final Subscriber<? super NotificationsResponse.Notification> subscriber) {
                        getProfileChannel().bind(EVENT_NEW_NOTIFICATION, new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                try {
                                    final NotificationsResponse.Notification notification = mGson
                                            .fromJson(data, NotificationsResponse.Notification.class);
                                    logMessage(notification, "profile / getNewNotificationObservable");
                                    subscriber.onNext(notification);
                                } catch (JsonSyntaxException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                });
    }

    public Observable<Stats> getStatsObservable() {
        return Observable
                .create(new Observable.OnSubscribe<Stats>() {
                    @Override
                    public void call(final Subscriber<? super Stats> subscriber) {
                        getProfileChannel().bind(EVENT_STATS_UPDATE, new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                try {
                                    final Stats pusherStats = mGson.getAdapter(Stats.class).fromJson(data);
                                    logMessage(pusherStats, "profile / getStatsObservable");
                                    subscriber.onNext(pusherStats);
                                } catch (IOException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .observeOn(uiScheduler);
    }

    private PresenceChannel getProfileChannel() {
        final String id = mUser.getId();
        log("get profile channel id : " + id + userLogInfo());

        final PresenceChannel presenceChannel = mPusher.getPresenceChannel(PusherHelper.getProfileChannelName(id));

        return presenceChannel == null ? subscribeProfileChannel() : presenceChannel;
    }

    public Observable<TypingInfo> getIsTypingObservable(@NonNull final PresenceChannel conversationChannel) {
        return Observable
                .create(new Observable.OnSubscribe<TypingInfo>() {
                    @Override
                    public void call(final Subscriber<? super TypingInfo> subscriber) {
                        conversationChannel.bind(EVENT_CLIENT_IS_TYPING, new PresenceChannelEventListenerAdapter() {

                            @Override
                            public void onEvent(String channelName, String eventName, String data) {
                                final TypingPusherModel typingPusherModel = mGson.fromJson(data, TypingPusherModel.class);
                                logMessage(typingPusherModel, "conversation / getIsTypingObservable");
                                subscriber.onNext(TypingInfo.typing(typingPusherModel.username));
                            }
                        });
                    }
                });
    }

    public void unsubscribeConversationChannel(@NonNull String conversationId) {
        log("unsubscribe conversation channel : " + conversationId + userLogInfo());
        mPusher.unsubscribe(getConversationChannelName(conversationId));
    }

    public void unsubscribeProfileChannel() {
        log("unsubscribe profile channel : " + mUser.getId() + userLogInfo());
        mPusher.unsubscribe(PusherHelper.getProfileChannelName(mUser.getId()));
    }

    @Nonnull
    public PresenceChannel subscribeConversationChannel(@NonNull String conversationId) {
        log("subscribe conversation channel : " + conversationId + userLogInfo());
        final PresenceChannel presenceChannel = mPusher.getPresenceChannel(PusherHelper.getConversationChannelName(conversationId));

        if (presenceChannel == null || !presenceChannel.isSubscribed()) {
            return mPusher.subscribePresence(PusherHelper.getConversationChannelName(conversationId));
        } else {
            return presenceChannel;
        }
    }

    public boolean shouldConnect() {
        return mPusher.getConnection().getState() != ConnectionState.CONNECTING && mPusher.getConnection().getState() != ConnectionState.CONNECTED;
    }

    private ConnectionEventListener getEventListener() {
        return new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
                Log.i(TAG, connectionStateChange.getCurrentState().name() + userLogInfo());
            }

            @Override
            public void onError(String s, String s1, Exception e) {
                Log.e(TAG, "pusher message" + userLogInfo(), e);
            }
        };
    }

    @NonNull
    private String userLogInfo() {
        return " user : " + mUser.getId() + " is page " + mUser.isPage();
    }

    public PresenceChannel subscribeProfileChannel() {
        final String id = mUser.getId();
        log("subscribe profile channel : " + id + userLogInfo());
        return mPusher.subscribePresence(PusherHelper.getProfileChannelName(id));
    }

    public void connect() {
        log("connect called : " + userLogInfo());
        mPusher.connect(getEventListener());
    }

    public void sendTyping(@NonNull String conversationId, @NonNull String userId, @NonNull String userName) {
        final PresenceChannel presenceChannel = mPusher.getPresenceChannel(getConversationChannelName(conversationId));
        if (presenceChannel != null && presenceChannel.isSubscribed() && mPusher.getConnection().getState() != ConnectionState.CONNECTING) {
            final String typing = mGson.toJson(new TypingPusherModel(userId, userName));
            presenceChannel.trigger("client-is_typing", typing);
        }
    }

    public boolean isInit() {
        return mPusher != null;
    }

    public void disconnect() {
        log("disconnect" + userLogInfo());
        mPusher.disconnect();
    }

    private static class TypingPusherModel {
        private final String id;
        private final String username;

        public TypingPusherModel(@NonNull String id, @NonNull String username) {
            this.id = id;
            this.username = username;
        }

        @Override
        public String toString() {
            return "TypingPusherModel{" +
                    "id='" + id + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }

    private void logMessage(Object message, String channel) {
        log(channel + " : " + message.toString() + userLogInfo());
    }

    private void log(String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, msg);
        }
    }
}
