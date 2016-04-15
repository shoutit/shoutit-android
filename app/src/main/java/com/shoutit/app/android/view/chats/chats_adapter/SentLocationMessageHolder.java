package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.models.SentLocationMessage;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SentLocationMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Nonnull
    private final View mItemView;
    @Bind(R.id.chats_sent_location_date_textview)
    TextView mChatsSentLocationDateTextview;

    public SentLocationMessageHolder(@Nonnull View itemView) {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final SentLocationMessage message = (SentLocationMessage) item;
        mChatsSentLocationDateTextview.setText(message.getTime());

        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.click();
            }
        });

    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view) {
        return new SentLocationMessageHolder(view);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_sent_location_message;
    }
}