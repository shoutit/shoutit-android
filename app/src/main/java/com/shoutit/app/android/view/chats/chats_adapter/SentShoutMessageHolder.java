package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.message_models.SentShoutMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SentShoutMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {


    private final Picasso mPicasso;
    @Bind(R.id.chats_sent_shout_image_imageview)
    ImageView mChatsSentShoutImageImageview;
    @Bind(R.id.chats_sent_shout_date_textview)
    TextView mChatsSentShoutDateTextview;
    @Bind(R.id.chats_message_shout_description)
    TextView mChatsMessageShoutDescription;
    @Bind(R.id.chats_shout_author)
    TextView mChatsShoutAuthor;
    @Bind(R.id.chats_shout_price)
    TextView mChatsShoutPrice;

    public SentShoutMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final SentShoutMessage message = (SentShoutMessage) item;

        mPicasso.load(message.getShoutImageUrl())
                .resizeDimen(R.dimen.chat_sent_shout_image_width, R.dimen.chat_sent_shout_image_height)
                .centerCrop()
                .into(mChatsSentShoutImageImageview);

        mChatsSentShoutDateTextview.setText(message.getTime());
        mChatsMessageShoutDescription.setText(message.getDescription());
        mChatsShoutAuthor.setText(message.getAuthor());
        mChatsShoutPrice.setText(message.getPrice());
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new SentShoutMessageHolder(view, picasso);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_sent_shout_message;
    }
}