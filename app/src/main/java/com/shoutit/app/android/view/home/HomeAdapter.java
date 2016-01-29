package com.shoutit.app.android.view.home;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.detector.ChangesDetector;
import com.appunite.detector.SimpleDetector;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Action1;

public class HomeAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_DISCOVER_CONTAINER = 0;
    private static final int VIEW_TYPE_SHOUT_HEADER = 1;
    private static final int VIEW_TYPE_SHOUT_ITEM = 2;

    @Nonnull
    private final HomeDiscoversAdapter homeDiscoversAdapter;
    @Nonnull
    private final Picasso picasso;

    @Nonnull
    private List<BaseAdapterItem> items = ImmutableList.of();

    @Inject
    public HomeAdapter(@ForActivity @Nonnull Context context,
                       @Nonnull HomeDiscoversAdapter homeDiscoversAdapter,
                       @Nonnull Picasso picasso) {
        super(context);
        this.homeDiscoversAdapter = homeDiscoversAdapter;
        this.picasso = picasso;
    }

    public class DiscoverContainerViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.DiscoverContainerAdapterItem> {
        @Bind(R.id.fragment_home_discover_recycler_view)
        RecyclerView recyclerView;

        public DiscoverContainerViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverContainerAdapterItem item) {
            recyclerView.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setAdapter(homeDiscoversAdapter);

            Observable.just(item.getAdapterItems())
                    .subscribe(homeDiscoversAdapter);
        }
    }

    public class ShoutHeaderViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.ShoutHeaderAdapterItem> {
        @Bind(R.id.home_shouts_header_tv)
        TextView headerTextView;

        public ShoutHeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.ShoutHeaderAdapterItem item) {
            headerTextView.setText(item.isUserLoggedIn() ?
                    context.getString(R.string.home_shouts_header_logged) :
                    context.getString(R.string.home_shouts_header_guest, item.getUserCity()));
        }
    }

    public class ShoutViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.ShoutAdapterItem> {
        @Bind(R.id.home_feed_card_image_view)
        ImageView cardImageView;
        @Bind(R.id.home_feed_card_title_tv)
        TextView titleTextView;
        @Bind(R.id.home_feed_card_name_tv)
        TextView nameTextView;
        @Bind(R.id.home_feed_card_price_tv)
        TextView cardPriceTextView;

        public ShoutViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.ShoutAdapterItem item) {
            final Shout shout = item.getShout();
            titleTextView.setText(shout.getTitle());
            nameTextView.setText(shout.getUser().getName());
            final String price = String.format(Locale.getDefault(), "%.1f", shout.getPrice());
            cardPriceTextView.setText(context.getString(
                    R.string.price_with_currency, price, shout.getCurrency())
            );

            picasso.load(shout.getThumbnail())
                    .fit()
                    .centerCrop()
                    .into(cardImageView);
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder<? extends BaseAdapterItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_DISCOVER_CONTAINER:
                return new DiscoverContainerViewHolder(layoutInflater.inflate(R.layout.home_discover_container_item, parent, false));
            case VIEW_TYPE_SHOUT_HEADER:
                return new ShoutHeaderViewHolder(layoutInflater.inflate(R.layout.home_feed_header_item, parent, false));
            case VIEW_TYPE_SHOUT_ITEM:
                return new ShoutViewHolder(layoutInflater.inflate(R.layout.home_feed_item, parent, false));
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
        if (item instanceof HomePresenter.DiscoverContainerAdapterItem) {
            return VIEW_TYPE_DISCOVER_CONTAINER;
        } else if (item instanceof HomePresenter.ShoutHeaderAdapterItem) {
            return VIEW_TYPE_SHOUT_HEADER;
        } else if (item instanceof HomePresenter.ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT_ITEM;
        } else {
            throw new RuntimeException("Unknown adapter view type");
        }
    }
}
