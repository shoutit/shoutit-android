package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.message_models.ReceivedImageMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReceivedImageMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Nonnull
    private final View mItemView;
    private final Picasso mPicasso;

    @Bind(R.id.cjats_received_image_imageview)
    ImageView mCjatsReceivedImageImageview;
    @Bind(R.id.cjats_received_image_textview)
    TextView mCjatsReceivedImageTextview;
    @Bind(R.id.chats_received_avatar)
    ImageView mChatsReceivedAvatar;

    public ReceivedImageMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mItemView = itemView;
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final ReceivedImageMessage message = (ReceivedImageMessage) item;
        mPicasso.load(message.getUrl())
                .fit()
                .centerCrop()
                .into(mCjatsReceivedImageImageview);
        mCjatsReceivedImageTextview.setText(message.getTime());

        AvatarHelper.setAvatar(message.isFirst(), message.getAvatarUrl(), mPicasso, mChatsReceivedAvatar);

        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.click();
            }
        });
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new ReceivedImageMessageHolder(view, picasso);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_received_image_message;
    }
}