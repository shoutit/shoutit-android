package com.shoutit.app.android.view.chats.chats_adapter;

import android.content.res.Resources;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.message_models.TypingItem;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TypingItemHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    private final Resources mResources;

    @Bind(R.id.chats_typing)
    TextView typingText;

    public TypingItemHolder(@Nonnull View itemView, Resources resources) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mResources = resources;
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final TypingItem typingItem = (TypingItem) item;
        typingText.setText(mResources.getString(R.string.chat_typing, typingItem.getUsername()));
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Resources resources) {
        return new TypingItemHolder(view, resources);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_typing_item;
    }
}