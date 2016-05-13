package com.shoutit.app.android.view.chats.message_models;

import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;

public abstract class ReceivedMessage extends BaseNoIDAdapterItem {

    private final boolean isFirst;
    private final String mAvatarUrl;

    public ReceivedMessage(boolean isFirst, String avatarUrl) {
        this.isFirst = isFirst;
        mAvatarUrl = avatarUrl;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }


}