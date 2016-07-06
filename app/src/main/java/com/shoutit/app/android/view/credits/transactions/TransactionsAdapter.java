package com.shoutit.app.android.view.credits.transactions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TransactionsAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;

    @Inject
    public TransactionsAdapter(@ForActivity @Nonnull Context context) {
        super(context);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TransactonViewHolder(mLayoutInflater.inflate(R.layout.transactions_item, parent, false));
    }

    class TransactonViewHolder extends ViewHolderManager.BaseViewHolder {

        @Nonnull
        private final View mItemView;

        @Bind(R.id.transactions_item_icon)
        ImageView mTransactionsItemIcon;
        @Bind(R.id.transactions_item_text)
        TextView mTransactionsItemText;
        @Bind(R.id.transactions_date)
        TextView mTransactionsDate;

        public TransactonViewHolder(@Nonnull View itemView) {
            super(itemView);
            mItemView = itemView;
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull BaseAdapterItem baseAdapterItem) {
            final TransactionsPresenter.TransactionItem item = (TransactionsPresenter.TransactionItem) baseAdapterItem;
            mTransactionsItemIcon.setImageResource(item.isOut() ? R.drawable.dollar_out : R.drawable.dollar_in);
            mTransactionsItemText.setText(item.getText());
            mTransactionsDate.setText(item.getTime());

            mItemView.setOnClickListener(v -> item.click());
        }
    }
}
