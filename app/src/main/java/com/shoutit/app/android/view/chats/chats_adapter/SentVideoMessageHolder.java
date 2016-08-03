package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.LayoutDirection;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.RtlUtils;
import com.shoutit.app.android.view.chats.message_models.SentVideoMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SentVideoMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Nonnull
    private final View mItemView;
    private final Picasso mPicasso;

    @Bind(R.id.chats_sent_video_imageview)
    ImageView mChatsSentVideoImageview;
    @Bind(R.id.chats_sent_video_textview)
    TextView mChatsSentVideoTextview;
    @Bind(R.id.chat_sent_video_container)
    ViewGroup container;

    public SentVideoMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mItemView = itemView;
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final SentVideoMessage message = (SentVideoMessage) item;

        RtlUtils.setLayoutDirection(container, LayoutDirection.LTR);

        mPicasso.load(message.getVideoThumbnail())
                .fit()
                .centerCrop()
                .into(mChatsSentVideoImageview);

        RtlUtils.setLayoutDirection(mChatsSentVideoTextview, LayoutDirection.LOCALE);
        mChatsSentVideoTextview.setText(message.getTime());

        mItemView.setOnClickListener(v -> message.click());
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new SentVideoMessageHolder(view, picasso);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_sent_video_message;
    }
}