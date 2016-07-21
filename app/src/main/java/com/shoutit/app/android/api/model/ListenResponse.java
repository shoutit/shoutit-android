package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class ListenResponse extends ApiMessageResponse {

    private final int newListenersCount;

    public ListenResponse(@NonNull String success, int newListenersCount) {
        super(success);
        this.newListenersCount = newListenersCount;
    }

    public int getNewListenersCount() {
        return newListenersCount;
    }
}
