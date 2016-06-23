package com.shoutit.app.android.view.pages;

import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import java.util.List;

public interface Listener {

    void showProgress(boolean show);

    void setData(@NonNull List<BaseAdapterItem> items);

    void showError(Throwable throwable);

}
