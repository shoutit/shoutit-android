package com.shoutit.app.android.view.chats.chat_info.chats_participants;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class ProfileItem implements BaseAdapterItem {

    private final String mId;
    private final String mName;
    private final String mImage;
    private final boolean mIsAdmin;
    private final boolean mIsBlocked;
    private final OnItemClicked onItemClicked;

    public ProfileItem(String id, String name, String image, boolean isAdmin, boolean isBlocked, OnItemClicked onItemClicked) {
        mId = id;
        mName = name;
        mImage = image;
        mIsAdmin = isAdmin;
        mIsBlocked = isBlocked;
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

    public boolean isAdmin() {
        return mIsAdmin;
    }

    public boolean isBlocked() {
        return mIsBlocked;
    }

    public void itemClicked() {
        onItemClicked.onItemClicked(mId, mIsBlocked, mIsAdmin, mName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ProfileItem that = (ProfileItem) o;

        if (mIsAdmin != that.mIsAdmin) return false;
        if (mIsBlocked != that.mIsBlocked) return false;
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
        result = 31 * result + (mIsAdmin ? 1 : 0);
        result = 31 * result + (mIsBlocked ? 1 : 0);
        result = 31 * result + (onItemClicked != null ? onItemClicked.hashCode() : 0);
        return result;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ProfileItem && ((ProfileItem) item).getId().equals(mId);
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
        void onItemClicked(String id, boolean isBlocked, boolean isAdmin, String name);
    }
}