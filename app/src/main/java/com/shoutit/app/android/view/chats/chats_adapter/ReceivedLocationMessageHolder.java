package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.message_models.ReceivedLocationMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReceivedLocationMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Nonnull
    private final View mItemView;
    private final Picasso mPicasso;

    @Bind(R.id.chats_received_location_textview)
    TextView mChatsReceivedLocationTextview;
    @Bind(R.id.chats_received_avatar)
    ImageView mChatsReceivedAvatar;

    private ReceivedLocationMessage item;

    public ReceivedLocationMessageHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        mItemView = itemView;
        mPicasso = picasso;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem adapterItem) {
        item = (ReceivedLocationMessage) adapterItem;
        mChatsReceivedLocationTextview.setText(item.getTime());

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
        return new ReceivedLocationMessageHolder(view, picasso);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_received_location_message;
    }
}