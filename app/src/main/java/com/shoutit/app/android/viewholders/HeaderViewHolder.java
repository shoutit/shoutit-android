package com.shoutit.app.android.viewholders;

import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HeaderViewHolder extends ViewHolderManager.BaseViewHolder<HeaderAdapterItem> {
    @Bind(R.id.header_item_tv)
    TextView titleTextView;

    public HeaderViewHolder(@Nonnull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull HeaderAdapterItem item) {
        titleTextView.setText(item.getTitle());
    }
}