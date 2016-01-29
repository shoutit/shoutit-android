package com.shoutit.app.android.view.home;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Discover;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeDiscoversAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    @Nonnull
    private final Picasso picasso;

    @Inject
    public HomeDiscoversAdapter(@Nonnull Picasso picasso,
                                @Nonnull @ForActivity Context context) {
        super(context);
        this.picasso = picasso;
    }

    class DiscoverHeaderViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.DiscoverHeaderAdapterItem> {
        @Bind(R.id.home_discover_header_tv)
        TextView headerTextView;

        public DiscoverHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverHeaderAdapterItem item) {
            headerTextView.setText(context.getString(R.string.home_discover_header, item.getCity()));
        }
    }

    class DiscoverItemViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.DiscoverAdapterItem> {
        @Bind(R.id.home_discover_item_iv)
        ImageView cardImageView;
        @Bind(R.id.home_discover_item_tv)
        TextView cardTitleTextView;

        public DiscoverItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverAdapterItem item) {
            final Discover discover = item.getDiscover();
            cardTitleTextView.setText(discover.getTitle());

            picasso.load(discover.getImage())
                    .fit()
                    .centerCrop()
                    .into(cardImageView);
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new DiscoverHeaderViewHolder(layoutInflater.inflate(R.layout.home_discover_header, parent, false));
            case VIEW_TYPE_ITEM:
                return new DiscoverItemViewHolder(layoutInflater.inflate(R.layout.home_discover_item, parent, false));
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
        if (item instanceof HomePresenter.DiscoverHeaderAdapterItem) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof HomePresenter.DiscoverAdapterItem) {
            return VIEW_TYPE_ITEM;
        } else {
            throw new RuntimeException("Unknown adapter view type");
        }
    }
}
