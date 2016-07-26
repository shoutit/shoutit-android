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
import butterknife.OnClick;

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

    private ReceivedShoutMessage item;

    public ReceivedShoutMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mItemView = itemView;
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem adapterItem) {
        item = (ReceivedShoutMessage) adapterItem;

        mPicasso.load(item.getShoutImageUrl())
                .resizeDimen(R.dimen.chat_sent_shout_image_width, R.dimen.chat_sent_shout_image_height)
                .placeholder(R.drawable.pattern_placeholder)
                .centerCrop()
                .into(mChatsReceivedShoutImageImageview);

        mChatsReceivedShoutDateTextview.setText(item.getTime());
        mChatsMessageShoutDescription.setText(item.getDescription());
        mChatsShoutAuthor.setText(item.getAuthor());
        mChatsShoutPrice.setText(item.getPrice());

        AvatarHelper.setAvatar(item.isFirst(), item.getAvatarUrl(), mPicasso, mChatsReceivedAvatar);

        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.click();
            }
        });
    }

    @OnClick(R.id.chats_received_avatar)
    public void onAvatarClicked() {
        item.onAvatarClicked();
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new ReceivedShoutMessageHolder(view, picasso);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_received_shout_message;
    }
}