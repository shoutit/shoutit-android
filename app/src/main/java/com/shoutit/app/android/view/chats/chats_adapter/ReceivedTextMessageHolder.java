package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.view.chats.message_models.ReceivedTextMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReceivedTextMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    private final Picasso mPicasso;
    @Bind(R.id.chats_received_avatar)
    ImageView mChatsReceivedAvatar;
    @Bind(R.id.chats_received_message_message_textview)
    TextView mChatsReceivedMessageMessageTextview;
    @Bind(R.id.chats_received_message_date_textview)
    TextView mChatsReceivedMessageDateTextview;

    private ReceivedTextMessage item;

    public ReceivedTextMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem adapterItem) {
        this.item = (ReceivedTextMessage) adapterItem;

        mChatsReceivedMessageMessageTextview.setText(item.getMessage());
        mChatsReceivedMessageDateTextview.setText(item.getTime());

        AvatarHelper.setAvatar(item.isFirst(), item.getAvatarUrl(), mPicasso, mChatsReceivedAvatar);
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new ReceivedTextMessageHolder(view, picasso);
    }

    @OnClick(R.id.chats_received_avatar)
    public void onAvatarClicked() {
        item.onAvatarClicked();
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_received_message_message;
    }
}