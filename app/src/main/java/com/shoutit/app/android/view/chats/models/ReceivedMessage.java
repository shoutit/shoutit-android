package com.shoutit.app.android.view.chats.models;

import com.appunite.rx.android.adapter.BaseAdapterItem;

public abstract class ReceivedMessage implements BaseAdapterItem {

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