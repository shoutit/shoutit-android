package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.models.ReceivedVideoMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReceivedVideoMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Nonnull
    private final View mItemView;
    private final Picasso mPicasso;

    @Bind(R.id.chats_received_avatar)
    ImageView mChatsReceivedAvatar;
    @Bind(R.id.chats_received_video_imageview)
    ImageView mChatsReceivedVideoImageview;
    @Bind(R.id.chats_received_video_textview)
    TextView mChatsReceivedVideoTextview;

    public ReceivedVideoMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mItemView = itemView;
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final ReceivedVideoMessage message = (ReceivedVideoMessage) item;

        AvatarHelper.setAvatar(message.isFirst(), message.getAvatarUrl(), mPicasso, mChatsReceivedAvatar);

        mPicasso.load(message.getVideoThumbnail())
                .fit()
                .centerCrop()
                .into(mChatsReceivedVideoImageview);
        mChatsReceivedVideoTextview.setText(message.getTime());

        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.click();
            }
        });
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new ReceivedVideoMessageHolder(view, picasso);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_received_video_message;
    }
}