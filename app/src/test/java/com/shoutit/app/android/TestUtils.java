package com.shoutit.app.android;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;

public class TestUtils {

    public static User getUser() {
        return new User("id", null, null, null, "userrname", null, null, null,
                false, null, null, false, false, false, null, 1, null,
                null, null, 1, null, false, null, null, null, null, null, null);
    }

    public static Shout getShout() {
        return new Shout("", "", "", "", new UserLocation(
                0, 0, "", "", "", "", ""), "", "", 0L, 0, "", "", "", getUser(),
                null, null, 0, null, null, 0, ImmutableList.<Conversation>of(), true, null, null);
    }
}
