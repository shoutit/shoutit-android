package com.shoutit.app.android.view.shout;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RelatedShoutsAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_SHOUT_ITEM = 1;
    private static final int VIEW_TYPE_SHOW_MORE = 2;

    @Nonnull
    private final Picasso picasso;

    @Inject
    public RelatedShoutsAdapter(@Nonnull Picasso picasso,
                                @Nonnull @ForActivity Context context) {
        super(context);
        this.picasso = picasso;
    }

    class ShowAllItemsViewHolder extends ViewHolderManager.BaseViewHolder<ShoutAdapterItems.SeeAllRelatesAdapterItem> {

        private ShoutAdapterItems.SeeAllRelatesAdapterItem item;

        public ShowAllItemsViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull ShoutAdapterItems.SeeAllRelatesAdapterItem item) {
            this.item = item;
        }

        @OnClick(R.id.shout_see_all_tv)
        public void onSeeAllClick() {
            item.onSeeAllClicked();
        }
    }

    class RelatedShoutViewHolder extends ViewHolderManager.BaseViewHolder<ShoutAdapterItem> implements View.OnClickListener {
        @Bind(R.id.shout_related_iv)
        ImageView shoutImageView;
        @Bind(R.id.shout_related_title_tv)
        TextView titleTextView;
        @Bind(R.id.shout_related_name_tv)
        TextView nameTextView;
        @Bind(R.id.shout_related_price_tv)
        TextView priceTextView;

        private ShoutAdapterItem item;

        public RelatedShoutViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull ShoutAdapterItem item) {
            this.item = item;
            final Shout shout = item.getShout();
            titleTextView.setText(shout.getTitle());
            nameTextView.setText(shout.getProfile().getName());
            final String price = PriceUtils.formatPriceWithCurrency(shout.getPrice(),
                    context.getResources(), shout.getCurrency());
            priceTextView.setText(price);

            picasso.load(shout.getThumbnail())
                    .placeholder(R.drawable.pattern_placeholder)
                    .fit()
                    .centerCrop()
                    .into(shoutImageView);
        }

        @Override
        public void onClick(View v) {
            item.onShoutSelected();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SHOW_MORE:
                return new ShowAllItemsViewHolder(layoutInflater.inflate(R.layout.shout_see_all_item, parent, false));
            case VIEW_TYPE_SHOUT_ITEM:
                return new RelatedShoutViewHolder(layoutInflater.inflate(R.layout.shout_releated_shout_item, parent, false));
            default:
                throw new RuntimeException("Unknown adapter view type");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof ShoutAdapterItems.SeeAllRelatesAdapterItem) {
            return VIEW_TYPE_SHOW_MORE;
        } else if (item instanceof ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT_ITEM;
        } else {
            throw new RuntimeException("Unknown adapter view type");
        }
    }
}
