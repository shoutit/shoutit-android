package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.message_models.ReceivedShoutMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReceivedShoutMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Nonnull
    private final View mItemView;
    private final Picasso mPicasso;
    @Bind(R.id.chats_received_avatar)
    ImageView mChatsReceivedAvatar;
    @Bind(R.id.chats_received_shout_image_imageview)
    ImageView mChatsReceivedShoutImageImageview;
    @Bind(R.id.chats_received_shout_date_textview)
    TextView mChatsReceivedShoutDateTextview;
    @Bind(R.id.chats_message_shout_description)
    TextView mChatsMessageShoutDescription;
    @Bind(R.id.chats_shout_author)
    TextView mChatsShoutAuthor;
    @Bind(R.id.chats_shout_price)
    TextView mChatsShoutPrice;

    public ReceivedShoutMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mItemView = itemView;
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final ReceivedShoutMessage message = (ReceivedShoutMessage) item;

        mPicasso.load(message.getShoutImageUrl())
                .resizeDimen(R.dimen.chat_sent_shout_image_width, R.dimen.chat_sent_shout_image_height)
                .centerCrop()
                .into(mChatsReceivedShoutImageImageview);

        mChatsReceivedShoutDateTextview.setText(message.getTime());
        mChatsMessageShoutDescription.setText(message.getDescription());
        mChatsShoutAuthor.setText(message.getAuthor());
        mChatsShoutPrice.setText(message.getPrice());

        AvatarHelper.setAvatar(message.isFirst(), message.getAvatarUrl(), mPicasso, mChatsReceivedAvatar);

        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.click();
            }
        });
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new ReceivedShoutMessageHolder(view, picasso);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_received_shout_message;
    }
}