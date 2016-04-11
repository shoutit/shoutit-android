package com.shoutit.app.android.view.chats;

import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.User;

import java.util.Set;

public class PresenceChannelEventListenerAdapter implements PresenceChannelEventListener {

    @Override
    public void onUsersInformationReceived(String channelName, Set<User> users) {

    }

    @Override
    public void userSubscribed(String channelName, User user) {

    }

    @Override
    public void userUnsubscribed(String channelName, User user) {

    }

    @Override
    public void onAuthenticationFailure(String message, Exception e) {

    }

    @Override
    public void onSubscriptionSucceeded(String channelName) {

    }

    @Override
    public void onEvent(String channelName, String eventName, String data) {

    }

}
