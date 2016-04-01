package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.NonNull;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.view.conversations.ConversationsPresenter;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;

public class SentVideoMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    private final Picasso mPicasso;

    public SentVideoMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final ConversationsPresenter.ConversationChatItem conversationChatItem = (ConversationsPresenter.ConversationChatItem) item;
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new SentVideoMessageHolder(view, picasso);
    }
}