package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.message_models.DateItem;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DateItemHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Bind(R.id.chate_date_item_textview)
    TextView mChateDateItemTextview;

    public DateItemHolder(@Nonnull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final DateItem dateItem = (DateItem) item;
        mChateDateItemTextview.setText(dateItem.getDate());
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view) {
        return new DateItemHolder(view);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_date_item;
    }
}