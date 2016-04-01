package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.message_models.ReceivedTextMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReceivedTextMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    private final Picasso mPicasso;
    @Bind(R.id.chats_received_avatar)
    ImageView mChatsReceivedAvatar;
    @Bind(R.id.chats_received_message_message_textview)
    TextView mChatsReceivedMessageMessageTextview;
    @Bind(R.id.chats_received_message_date_textview)
    TextView mChatsReceivedMessageDateTextview;

    public ReceivedTextMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final ReceivedTextMessage message = (ReceivedTextMessage) item;
        mChatsReceivedMessageMessageTextview.setText(message.getMessage());
        mChatsReceivedMessageDateTextview.setText(message.getTime());

        AvatarHelper.setAvatar(message.isFirst(), message.getAvatarUrl(), mPicasso, mChatsReceivedAvatar);
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new ReceivedTextMessageHolder(view, picasso);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_received_message_message;
    }
}