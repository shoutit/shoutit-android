package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.message_models.SentImageMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SentImageMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Nonnull
    private final View mItemView;
    private final Picasso mPicasso;

    @Bind(R.id.cjats_sent_image_imageview)
    ImageView mCjatsSentImageImageview;
    @Bind(R.id.cjats_sent_image_textview)
    TextView mCjatsSentImageTextview;

    public SentImageMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mItemView = itemView;
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final SentImageMessage message = (SentImageMessage) item;
        mPicasso.load(message.getUrl())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.pattern_placeholder)
                .error(R.drawable.pattern_placeholder)
                .into(mCjatsSentImageImageview);
        mCjatsSentImageTextview.setText(message.getTime());

        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.click();
            }
        });
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
        return new SentImageMessageHolder(view, picasso);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_sent_image_message;
    }
}