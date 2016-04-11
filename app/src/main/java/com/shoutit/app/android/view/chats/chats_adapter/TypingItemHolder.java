package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;

import javax.annotation.Nonnull;

public class TypingItemHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    public TypingItemHolder(@Nonnull View itemView) {
        super(itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view) {
        return new TypingItemHolder(view);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_typing_item;
    }
}