package com.shoutit.app.android.view.discover;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PriceUtils;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiscoverAdapter extends BaseAdapter {

    public static final int VIEW_TYPE_HEADER = 1;
    public static final int VIEW_TYPE_DISCOVER = 2;
    public static final int VIEW_TYPE_SHOUT_HEADER = 3;
    public static final int VIEW_TYPE_SHOUT = 4;
    public static final int VIEW_TYPE_BUTTON = 5;

    @Nonnull
    private final Picasso picasso;

    @Inject
    public DiscoverAdapter(@ForActivity @Nonnull Context context,
                           @Nonnull Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    class HeaderViewHolder extends ViewHolderManager.BaseViewHolder<DiscoverPresenter.HeaderAdapterItem> {
        @Bind(R.id.discover_header_title)
        TextView titleTextView;
        @Bind(R.id.discover_header_iv)
        ImageView cardImageView;

        public HeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
        }

        @Override
        public void bind(@Nonnull DiscoverPresenter.HeaderAdapterItem item) {
            picasso.load(item.getImage())
                    .placeholder(R.drawable.pattern_bg)
                    .error(R.drawable.pattern_bg)
                    .fit()
                    .centerCrop()
                    .into(cardImageView);

            titleTextView.setText(item.getTitle());
        }
    }

    class DiscoverViewHolder extends ViewHolderManager.BaseViewHolder<DiscoverPresenter.DiscoverAdapterItem> implements View.OnClickListener {
        @Bind(R.id.discover_item_iv)
        ImageView imageView;
        @Bind(R.id.discover_item_tv)
        TextView titleTextView;
        private DiscoverPresenter.DiscoverAdapterItem item;


        public DiscoverViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull DiscoverPresenter.DiscoverAdapterItem item) {
            this.item = item;
            final DiscoverChild discover =  item.getDiscoverChild();

            picasso.load(discover.getImage())
                    .placeholder(R.drawable.pattern_placeholder)
                    .error(R.drawable.pattern_placeholder)
                    .fit()
                    .centerCrop()
                    .into(imageView);

            titleTextView.setText(discover.getTitle());
        }

        @Override
        public void onClick(View v) {
            item.onDiscoverSelected();
        }
    }

    class ShoutHeaderViewHolder extends ViewHolderManager.BaseViewHolder<DiscoverPresenter.ShoutHeaderAdapterItem> {

        public ShoutHeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
        }

        @Override
        public void bind(@Nonnull DiscoverPresenter.ShoutHeaderAdapterItem item) {

        }
    }

    class ShoutViewHolder extends ViewHolderManager.BaseViewHolder<DiscoverPresenter.ShoutAdapterItem> {
        @Bind(R.id.discover_shout_card_image_view)
        ImageView cardImageView;
        @Bind(R.id.discover_shout_card_title_tv)
        TextView titleTextView;
        @Bind(R.id.discover_shout_card_name_tv)
        TextView shoutNameTextView;
        @Bind(R.id.discover_shout_card_price_tv)
        TextView shoutPriceTextView;

        public ShoutViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
        }

        @Override
        public void bind(@Nonnull DiscoverPresenter.ShoutAdapterItem item) {
            final Shout shout = item.getShout();

            picasso.load(shout.getThumbnail())
                    .placeholder(R.drawable.pattern_placeholder)
                    .error(R.drawable.pattern_placeholder)
                    .fit()
                    .centerCrop()
                    .into(cardImageView);

            titleTextView.setText(shout.getTitle());
            shoutNameTextView.setText(shout.getText());
            final String price = PriceUtils.formatPrice(shout.getPrice());
            shoutPriceTextView.setText(context.getString(
                    R.string.price_with_currency, price, shout.getCurrency()));
        }
    }



    class ButtonViewHolder extends ViewHolderManager.BaseViewHolder<DiscoverPresenter.ShowMoreButtonAdapterItem> implements View.OnClickListener {

        private DiscoverPresenter.ShowMoreButtonAdapterItem item;

        public ButtonViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull DiscoverPresenter.ShowMoreButtonAdapterItem item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            item.showMoreClicked();
        }
    }


    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(layoutInflater.inflate(R.layout.discover_header_item, parent, false));
            case VIEW_TYPE_DISCOVER:
                return new DiscoverViewHolder(layoutInflater.inflate(R.layout.discover_item, parent, false));
            case VIEW_TYPE_SHOUT_HEADER:
                return new ShoutHeaderViewHolder(layoutInflater.inflate(R.layout.discover_shouts_header, parent, false));
            case VIEW_TYPE_SHOUT:
                return new ShoutViewHolder(layoutInflater.inflate(R.layout.discover_shout_item, parent, false));
            case VIEW_TYPE_BUTTON:
                return new ButtonViewHolder(layoutInflater.inflate(R.layout.discover_see_all_button_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);

        if (item instanceof DiscoverPresenter.HeaderAdapterItem) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof DiscoverPresenter.DiscoverAdapterItem) {
            return VIEW_TYPE_DISCOVER;
        } else if (item instanceof DiscoverPresenter.ShoutHeaderAdapterItem) {
            return VIEW_TYPE_SHOUT_HEADER;
        } else if (item instanceof DiscoverPresenter.ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else if (item instanceof DiscoverPresenter.ShowMoreButtonAdapterItem) {
            return VIEW_TYPE_BUTTON;
        } else {
            throw new RuntimeException("Unknown view type");
        }
    }
}
