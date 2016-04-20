package com.shoutit.app.android.utils;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.util.HttpAuthorizer;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.PusherMessage;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.model.Stats;
import com.shoutit.app.android.view.chats.PresenceChannelEventListenerAdapter;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;

public class PusherHelper {

    private static final String NEW_MESSAGE = "new_message";
    private static final String STATS_UPDATE = "stats_update";

    private static final String DEBUG_KEY = "7bee1e468fabb6287fc5";
    private static final String PRODUCTION_KEY = "86d676926d4afda44089";

    private static final String PROFILE_CHANNEL = "presence-v3-p-%1$s";

    private Pusher mPusher;
    private final Gson mGson;
    private final UserPreferences mUserPreferences;

    public PusherHelper(@NonNull Gson gson, UserPreferences userPreferences) {
        mGson = gson;
        mUserPreferences = userPreferences;
    }

    public void init(@NonNull String token) {
        final HttpAuthorizer authorizer = new HttpAuthorizer(BuildConfig.API_URL + "pusher/auth");
        authorizer.setHeaders(ImmutableMap.of("Authorization", "Bearer " + token));
        final PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        mPusher = new Pusher(BuildConfig.DEBUG ? DEBUG_KEY : PRODUCTION_KEY, options);
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

    public PresenceChannel getProfileChannel() {
        final User user = mUserPreferences.getUser();
        assert user != null;
        return mPusher.getPresenceChannel(PusherHelper.getProfileChannelName(user.getId()));
    }
}
