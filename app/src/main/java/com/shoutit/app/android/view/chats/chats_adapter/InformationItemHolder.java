package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.chats.models.InfoItem;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InformationItemHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Bind(R.id.chate_date_item_textview)
    TextView mChateDateItemTextview;

    public InformationItemHolder(@Nonnull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final InfoItem infoItem = (InfoItem) item;
        mChateDateItemTextview.setText(infoItem.getInfo());
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view) {
        return new InformationItemHolder(view);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_info_item;
    }
}