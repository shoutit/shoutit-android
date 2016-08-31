package com.shoutit.app.android.view.discover;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Strings;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.BaseShoutAdapterItem;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.viewholders.ShoutViewHolder;
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

        private final int shadowYOffset;
        private final int shadowRadius;
        private final int shadowXOffset;

        public HeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            shadowRadius = context.getResources().getDimensionPixelSize(R.dimen.discover_shadow_radius);
            shadowYOffset = context.getResources().getDimensionPixelSize(R.dimen.discover_shadow_y_offset);
            shadowXOffset = context.getResources().getDimensionPixelSize(R.dimen.discover_shadow_x_offset);
        }

        @Override
        public void bind(@Nonnull DiscoverPresenter.HeaderAdapterItem item) {

            if (TextUtils.isEmpty(item.getImage())) {
                titleTextView.setTextColor(context.getResources().getColor(R.color.black_54));
                titleTextView.setShadowLayer(0, 0, 0, context.getResources().getColor(R.color.black_87));
            } else {
                titleTextView.setTextColor(context.getResources().getColor(android.R.color.white));
                titleTextView.setShadowLayer(shadowRadius, shadowXOffset, shadowYOffset, context.getResources().getColor(R.color.black_87));
            }

            titleTextView.setText(item.getTitle());
        }
    }

    class DiscoverViewHolder extends ViewHolderManager.BaseViewHolder<DiscoverPresenter.DiscoverAdapterItem> implements View.OnClickListener {
        @Bind(R.id.discover_card_image)
        ImageView imageView;
        @Bind(R.id.discover_card_title)
        TextView titleTextView;
        @Bind(R.id.discover_card_shouts)
        TextView subTitleTv;

        private DiscoverPresenter.DiscoverAdapterItem item;


        public DiscoverViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull DiscoverPresenter.DiscoverAdapterItem item) {
            this.item = item;
            final DiscoverChild discover = item.getDiscoverChild();

            picasso.load(Strings.emptyToNull(discover.getImage()))
                    .placeholder(R.drawable.pattern_placeholder)
                    .error(R.drawable.pattern_placeholder)
                    .fit()
                    .centerCrop()
                    .into(imageView);

            titleTextView.setText(discover.getTitle());
            subTitleTv.setText(discover.getSubtitle());
        }

        @Override
        public void onClick(View v) {
            item.onDiscoverSelected();
        }
    }

    class ShoutHeaderViewHolder extends ViewHolderManager.BaseViewHolder<DiscoverPresenter.ShoutHeaderAdapterItem> {

        public ShoutHeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull DiscoverPresenter.ShoutHeaderAdapterItem item) {

        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(layoutInflater.inflate(R.layout.discover_header_item, parent, false));
            case VIEW_TYPE_DISCOVER:
                return new DiscoverViewHolder(layoutInflater.inflate(R.layout.discover_card, parent, false));
            case VIEW_TYPE_SHOUT_HEADER:
                return new ShoutHeaderViewHolder(layoutInflater.inflate(R.layout.discover_shouts_header, parent, false));
            case VIEW_TYPE_SHOUT:
                return new ShoutViewHolder(layoutInflater.inflate(ShoutGridViewHolder.getLayoutRes(), parent, false), picasso);
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
        } else if (item instanceof BaseShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else {
            throw new RuntimeException("Unknown view type");
        }
    }
}
