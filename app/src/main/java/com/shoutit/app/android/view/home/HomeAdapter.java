package com.shoutit.app.android.view.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.appunite.rx.dagger.UiScheduler;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.DateTimeUtils;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class HomeAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_DISCOVER_HEADER = 1;
    private static final int VIEW_TYPE_DISCOVER_ITEMS_CONTAINER = 2;
    private static final int VIEW_TYPE_SHOUT_HEADER = 3;
    public static final int VIEW_TYPE_SHOUT_ITEM = 4;
    private static final int POSITION_WHERE_SHOUTS_ITEMS_STARTS = 3;

    @Nonnull
    private final HomeDiscoversAdapter homeDiscoversAdapter;
    @Nonnull
    private final Picasso picasso;
    @Nonnull
    private final Scheduler uiScheduler;
    private boolean isLinearLayoutManager = true;

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

    public void switchLayoutManager() {
        isLinearLayoutManager = !isLinearLayoutManager;
        notifyItemRangeChanged(POSITION_WHERE_SHOUTS_ITEMS_STARTS,
                getItemCount() - POSITION_WHERE_SHOUTS_ITEMS_STARTS + 1);
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

        private Subscription subscription;
        private final RecyclerView.ItemDecoration itemDecoration;
        private final int itemSpacing;

        public DiscoverContainerViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemSpacing = context.getResources().getDimensionPixelOffset(R.dimen.home_discover_item_spacing);
            itemDecoration = new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.left = itemSpacing;
                    outRect.right = itemSpacing;
                }
            };
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverContainerAdapterItem item) {
            recycle();

            final MyLinearLayoutManager layoutManager = new MyLinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.addItemDecoration(itemDecoration);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(homeDiscoversAdapter);

            subscription = Observable.just(item.getAdapterItems())
                            .observeOn(uiScheduler)
                            .subscribe(homeDiscoversAdapter);
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
        @Bind(R.id.home_filter_iv)
        View filterIcon;
        @Bind(R.id.home_switch_cb)
        CheckBox layoutManagerSwitchView;

        private CompositeSubscription subscription;

        public ShoutHeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.ShoutHeaderAdapterItem item) {
            recycle();

            headerTextView.setText(item.isUserLoggedIn() ?
                    context.getString(R.string.home_shouts_header_logged) :
                    context.getString(R.string.home_shouts_header_guest, item.getUserCity()));

            layoutManagerSwitchView.setChecked(isLinearLayoutManager);

            subscription = new CompositeSubscription(
                    RxView.clicks(layoutManagerSwitchView)
                            .subscribe(item.getLayoutManagerSwitchObserver()),

                    RxView.clicks(filterIcon)
                            .observeOn(uiScheduler)
                            .subscribe(new Action1<Void>() {
                                @Override
                                public void call(Void aVoid) {
                                    Toast.makeText(context, "Not implemented yet", Toast.LENGTH_LONG).show();
                                }
                            })
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

    class ShoutGridViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.ShoutAdapterItem> {
        @Bind(R.id.home_feed_card_image_view)
        ImageView cardImageView;
        @Bind(R.id.home_feed_card_title_tv)
        TextView titleTextView;
        @Bind(R.id.home_feed_card_name_tv)
        TextView nameTextView;
        @Bind(R.id.home_feed_card_price_tv)
        TextView cardPriceTextView;

        public ShoutGridViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.ShoutAdapterItem item) {
            final Shout shout = item.getShout();
            titleTextView.setText(shout.getTitle());
            nameTextView.setText(shout.getUser().getName());
            final String price = PriceUtils.formatPrice(shout.getPrice());
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

    class ShoutLinerViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.ShoutAdapterItem> {
        @Bind(R.id.home_feed_card_image_view)
        ImageView cardImageView;
        @Bind(R.id.home_feed_card_title_tv)
        TextView titleTextView;
        @Bind(R.id.home_feed_card_user_tv)
        TextView nameTextView;
        @Bind(R.id.home_feed_card_price_tv)
        TextView cardPriceTextView;
        @Bind(R.id.home_feed_card_chat_iv)
        View chatIcon;
        @Bind(R.id.home_feed_card_item_icon_iv)
        ImageView itemCategoryImageView;
        @Bind(R.id.home_feed_card_type_label_tv)
        TextView typeLabelTextView;
        @Bind(R.id.home_feed_card_country_iv)
        ImageView countryImageView;

        private CompositeSubscription subscription;

        public ShoutLinerViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.ShoutAdapterItem item) {
            recycle();

            final Shout shout = item.getShout();

            titleTextView.setText(shout.getTitle());

            final String timeAgo = DateTimeUtils.timeAgoFromSecondsToWeek(context, shout.getDatePublishedInMillis());
            nameTextView.setText(context.getString(R.string.home_user_and_date, shout.getUser().getName(), timeAgo));

            final String price = PriceUtils.formatPrice(shout.getPrice());
            cardPriceTextView.setText(context.getString(
                    R.string.price_with_currency, price, shout.getCurrency())
            );

            typeLabelTextView.setText(shout.getTypeResId());

            if (!TextUtils.isEmpty(shout.getThumbnail())) {
                picasso.load(shout.getThumbnail())
                        .placeholder(R.drawable.pattern_placeholder)
                        .fit()
                        .centerCrop()
                        .into(cardImageView);
            }

            if (shout.getCategory() != null && shout.getCategory().getMainTag() != null) {
                final String categoryImageUrl = shout.getCategory().getMainTag().getImage();
                if (!TextUtils.isEmpty(categoryImageUrl)) {
                    picasso.load(categoryImageUrl)
                            .fit()
                            .centerInside()
                            .into(itemCategoryImageView);
                }
            }

            if (shout.getLocation() != null && !TextUtils.isEmpty(shout.getLocation().getCountry())) {
                final String countryCode = shout.getLocation().getCountry().toLowerCase();
                final int flagResId = context.getResources().getIdentifier(countryCode,
                        "drawable", context.getPackageName());
                if (flagResId != 0) {
                    final Target target = PicassoHelper.getRoundedBitmapTarget(context, countryImageView);
                    cardImageView.setTag(target);
                    picasso.load(flagResId)
                            .resizeDimen(R.dimen.home_country_icon, R.dimen.home_country_icon)
                            .into(target);
                }
            }

            subscription = new CompositeSubscription(
                    RxView.clicks(chatIcon)
                    .observeOn(uiScheduler)
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            Toast.makeText(context, "Not impelmented yet", Toast.LENGTH_LONG).show();
                        }
                    })
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
                return isLinearLayoutManager ?
                        new ShoutLinerViewHolder(layoutInflater.inflate(R.layout.home_feed_item_linear, parent, false)) :
                        new ShoutGridViewHolder(layoutInflater.inflate(R.layout.home_feed_item_grid, parent, false));
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
