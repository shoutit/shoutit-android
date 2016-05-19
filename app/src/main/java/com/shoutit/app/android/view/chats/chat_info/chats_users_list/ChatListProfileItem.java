package com.shoutit.app.android.view.chats.chat_info.chats_users_list;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class ChatListProfileItem implements BaseAdapterItem {

    private final String mId;
    private final String mName;
    private final String mImage;
    private final OnItemClicked onItemClicked;

    public ChatListProfileItem(String id, String name, String image, OnItemClicked onItemClicked) {
        mId = id;
        mName = name;
        mImage = image;
        this.onItemClicked = onItemClicked;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getImage() {
        return mImage;
    }

    public void itemClicked() {
        onItemClicked.onItemClicked(mId, mName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ChatListProfileItem that = (ChatListProfileItem) o;

        if (mId != null ? !mId.equals(that.mId) : that.mId != null) return false;
        if (mName != null ? !mName.equals(that.mName) : that.mName != null) return false;
        if (mImage != null ? !mImage.equals(that.mImage) : that.mImage != null) return false;
        return onItemClicked != null ? onItemClicked.equals(that.onItemClicked) : that.onItemClicked == null;
    }

    @Override
    public int hashCode() {
        int result = mId != null ? mId.hashCode() : 0;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mImage != null ? mImage.hashCode() : 0);
        result = 31 * result + (onItemClicked != null ? onItemClicked.hashCode() : 0);
        return result;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ChatListProfileItem && ((ChatListProfileItem) item).getId().equals(mId);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item.equals(this);
    }

    @Override
    public long adapterId() {
        return hashCode();
    }

    public interface OnItemClicked {
        void onItemClicked(String id, String name);
    }
}