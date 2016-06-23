package com.shoutit.app.android.utils.adapter;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;

import javax.annotation.Nonnull;

public abstract class ClassViewHolderManager<T extends BaseAdapterItem> implements ViewHolderManager {

    public abstract class ViewBinder {

        public abstract void bind(T t);
    }

    private final Class<T> clazz;
    private final int mLayoutRes;

    public ClassViewHolderManager(Class<T> clazz, @LayoutRes int layoutRes) {
        this.clazz = clazz;
        mLayoutRes = layoutRes;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
        return clazz.isInstance(baseAdapterItem);
    }

    @Nonnull
    @Override
    public BaseViewHolder createViewHolder(@Nonnull ViewGroup viewGroup, @Nonnull LayoutInflater layoutInflater) {
        final View itemView = layoutInflater.inflate(mLayoutRes, viewGroup, false);
        final ViewBinder viewBinder = createViewBinder(itemView);
        return new ViewHolder(itemView, viewBinder);
    }

    public abstract ViewBinder createViewBinder(View view);

    private class ViewHolder extends BaseViewHolder<T> {

        private final ViewBinder mViewBinder;

        public ViewHolder(@Nonnull View itemView, ViewBinder viewBinder) {
            super(itemView);
            mViewBinder = viewBinder;
        }

        @Override
        public void bind(@Nonnull T t) {
            mViewBinder.bind(t);
        }
    }
}
