package com.shoutit.app.android.view.home;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.appunite.rx.dagger.UiScheduler;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;

public class HomeAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_DISCOVER_HEADER = 1;
    private static final int VIEW_TYPE_DISCOVER_ITEMS_CONTAINER = 2;
    private static final int VIEW_TYPE_SHOUT_HEADER = 3;
    public static final int VIEW_TYPE_SHOUT_ITEM = 4;

    @Nonnull
    private final HomeDiscoversAdapter homeDiscoversAdapter;
    @Nonnull
    private final Picasso picasso;
    @Nonnull
    private final Scheduler uiScheduler;

    @Inject
    public HomeAdapter(@ForActivity @Nonnull Context context,
                       @Nonnull HomeDiscoversAdapter homeDiscoversAdapter,
                       @Nonnull Picasso picasso,
                       @Nonnull @UiScheduler Scheduler uiScheduler) {
        super(context);
        this.homeDiscoversAdapter = homeDiscoversAdapter;
        this.picasso = picasso;
        this.uiScheduler = uiScheduler;
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

    class DiscoverContainerViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.DiscoverContainerAdapterItem> {
        @Bind(R.id.fragment_home_discover_recycler_view)
        RecyclerView recyclerView;

        private CompositeSubscription subscription;

        public DiscoverContainerViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverContainerAdapterItem item) {
            recycle();

            final LinearLayoutManager layoutManager = new LinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(homeDiscoversAdapter);

            subscription = new CompositeSubscription(
                    RxRecyclerView.scrollEvents(recyclerView)
                            .observeOn(uiScheduler)
                            .filter(LoadMoreHelper.needLoadMore(layoutManager, homeDiscoversAdapter))
                            .subscribe(item.getLoadMoreDiscoversObserver()),

                    Observable.just(item.getAdapterItems())
                            .observeOn(uiScheduler)
                            .subscribe(homeDiscoversAdapter)
            );
        }

        @Override
        public void onViewRecycled() {
            recycle();
            super.onViewRecycled();
        }

        private void recycle() {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    class ShoutHeaderViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.ShoutHeaderAdapterItem> {
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

    class ShoutViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.ShoutAdapterItem> {
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

            if (!TextUtils.isEmpty(shout.getThumbnail())) {
                picasso.load(shout.getThumbnail())
                        .placeholder(R.drawable.pattern_placeholder)
                        .fit()
                        .centerCrop()
                        .into(cardImageView);
            }

        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder<? extends BaseAdapterItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_DISCOVER_HEADER:
                return new DiscoverHeaderViewHolder(layoutInflater.inflate(R.layout.home_discover_header, parent, false));
            case VIEW_TYPE_DISCOVER_ITEMS_CONTAINER:
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
        if (item instanceof HomePresenter.DiscoverHeaderAdapterItem) {
            return VIEW_TYPE_DISCOVER_HEADER;
        } else if (item instanceof HomePresenter.DiscoverContainerAdapterItem) {
            return VIEW_TYPE_DISCOVER_ITEMS_CONTAINER;
        } else if (item instanceof HomePresenter.ShoutHeaderAdapterItem) {
            return VIEW_TYPE_SHOUT_HEADER;
        } else if (item instanceof HomePresenter.ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT_ITEM;
        } else {
            throw new RuntimeException("Unknown adapter view type");
        }
    }
}
