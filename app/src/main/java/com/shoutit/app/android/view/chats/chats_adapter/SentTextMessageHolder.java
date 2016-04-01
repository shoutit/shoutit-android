package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.NonNull;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.view.conversations.ConversationsPresenter;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;

public class SentTextMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    public SentTextMessageHolder(@Nonnull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final ConversationsPresenter.ConversationChatItem conversationChatItem = (ConversationsPresenter.ConversationChatItem) item;
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view) {
        return new SentTextMessageHolder(view);
    }
}