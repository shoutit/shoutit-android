package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.message_models.SentTextMessage;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SentTextMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Bind(R.id.chats_sent_message_message_textview)
    TextView mChatsSentMessageMessageTextview;
    @Bind(R.id.chats_sent_message_date_textview)
    TextView mChatsSentMessageDateTextview;

    public SentTextMessageHolder(@Nonnull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final SentTextMessage message = (SentTextMessage) item;
        mChatsSentMessageMessageTextview.setText(message.getMessage());
        mChatsSentMessageDateTextview.setText(message.getTime());
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view) {
        return new SentTextMessageHolder(view);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_sent_message_message;
    }
}