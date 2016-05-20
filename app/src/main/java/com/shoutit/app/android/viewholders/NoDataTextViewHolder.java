package com.shoutit.app.android.viewholders;

import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NoDataTextViewHolder extends ViewHolderManager.BaseViewHolder<NoDataTextAdapterItem> {

    @Bind(R.id.placeholder_text_tv)
    TextView textTv;

    public NoDataTextViewHolder(@Nonnull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull NoDataTextAdapterItem item) {
        textTv.setText(item.getTextToDisplay());
    }
}

