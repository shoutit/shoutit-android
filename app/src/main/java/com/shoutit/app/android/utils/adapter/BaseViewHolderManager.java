package com.shoutit.app.android.utils.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;

import javax.annotation.Nonnull;

public class BaseViewHolderManager<T extends BaseAdapterItem> implements ViewHolderManager {

    public interface ViewHolderFactory<TT extends BaseAdapterItem> {
        @NonNull BaseViewHolder<TT> createViewHolder(@NonNull View view);
    }

    @NonNull
    private final Class<? extends T> clazz;
    @LayoutRes
    private final int mLayoutRes;
    @NonNull
    private final ViewHolderFactory<T> mViewHolderFactory;

    public BaseViewHolderManager(@LayoutRes int layoutRes, @NonNull ViewHolderFactory<T> viewHolderFactory, @NonNull Class<? extends T> classList) {
        this.clazz = classList;
        mLayoutRes = layoutRes;
        mViewHolderFactory = viewHolderFactory;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
        return clazz.isInstance(baseAdapterItem);
    }

    @Nonnull
    @Override
    public BaseViewHolder createViewHolder(@Nonnull ViewGroup viewGroup, @Nonnull LayoutInflater layoutInflater) {
        final View itemView = layoutInflater.inflate(mLayoutRes, viewGroup, false);
        return mViewHolderFactory.createViewHolder(itemView);
    }
}
