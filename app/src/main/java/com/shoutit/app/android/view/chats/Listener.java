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
}