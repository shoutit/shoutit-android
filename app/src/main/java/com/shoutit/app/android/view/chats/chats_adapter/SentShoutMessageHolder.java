package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.NonNull;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.view.conversations.ConversationsPresenter;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;

public class SentShoutMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {


    public SentShoutMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final ConversationsPresenter.ConversationChatItem conversationChatItem = (ConversationsPresenter.ConversationChatItem) item;
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new SentShoutMessageHolder(view, picasso);
    }
}