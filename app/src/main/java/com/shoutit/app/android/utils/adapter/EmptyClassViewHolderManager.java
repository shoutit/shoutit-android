package com.shoutit.app.android.utils.adapter;

import android.support.annotation.LayoutRes;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;

public class EmptyClassViewHolderManager<T extends BaseAdapterItem> extends ClassViewHolderManager {

    public EmptyClassViewHolderManager(Class<T> clazz, @LayoutRes int layoutRes) {
        super(clazz, layoutRes);
    }

    @Override
    public ViewBinder createViewBinder(View view) {
        return new ViewBinder() {
            @Override
            public void bind(BaseAdapterItem baseAdapterItem) {

            }
        };
    }
}
