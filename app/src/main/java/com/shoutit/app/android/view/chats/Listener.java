package com.shoutit.app.android.view.chats;

import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import java.util.List;

public interface Listener {

    void emptyList();

    void showProgress(boolean show);

    void setData(@NonNull List<BaseAdapterItem> items);

    void error(Throwable throwable);

    void onVideoClicked(String url);

    void onLocationClicked(double latitude, double longitude);

    void onImageClicked(String url);

    void conversationDeleted();

    void setAboutShoutData(String title, String thumbnail, String type, String price, String authorAndTime, String id);

    void onShoutClicked(String shoutId);

    void onProfileClicked(String userName);

    void hideAttatchentsMenu();

    void setToolbarInfo(String title, String subTitle);
}