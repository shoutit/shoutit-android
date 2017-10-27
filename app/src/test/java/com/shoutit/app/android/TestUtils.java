package com.shoutit.app.android;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.ConversationDetails;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

public class TestUtils {

    public static User getUser() {
        return new User("id", null, null, null, "userrname", null, null, null,
                false, null, null, false, false, false, null, 1,
                null, 1, null, false, null, null, null, null, null, null, null, null, null, null, false);
    }

    public static Shout getShout() {
        return new Shout("", "", "", "", new UserLocation(
                0, 0, "", "", "", "", ""), "", "", 0L, 0, "", "", "", getUser(),
                null, null, 0,false, null, null, 0, ImmutableList.<ConversationDetails>of(), true, null, null, null, false, false);
    }

    public static ResponseBody getResponseBody() {
        return ResponseBody.create(MediaType.parse(""), "c");
    }
}
