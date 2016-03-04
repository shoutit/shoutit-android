package com.shoutit.app.android.view.home;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.appunite.rx.dagger.UiScheduler;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.view.shouts.ShoutLinerViewHolder;
import com.squareup.picasso.Picasso;

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
    public static final int VIEW_TYPE_EMPTY_SHOUTS_ITEM = 5;

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

    public void switchLayoutManager(boolean isLinearLayoutManager) {
        this.isLinearLayoutManager = isLinearLayoutManager;
        notifyDataSetChanged();
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
            final MyLinearLayoutManager layoutManager = new MyLinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.addItemDecoration(itemDecoration);
            recyclerView.setLayoutManager(layoutManager);
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverContainerAdapterItem item) {
            recycle();

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
        @Bind(R.id.home_switch_iv)
        ImageView layoutManagerSwitchView;

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

            layoutManagerSwitchView.setImageDrawable(context.getResources().getDrawable(
                    isLinearLayoutManager ? R.drawable.ic_grid_switch : R.drawable.ic_list_switch));

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

    class ShoutEmptyViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.ShoutsEmptyAdapterItem> {
        public ShoutEmptyViewHolder(@Nonnull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.ShoutsEmptyAdapterItem item) {
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
                        new ShoutLinerViewHolder(layoutInflater.inflate(R.layout.home_feed_item_linear, parent, false), context, picasso) :
                        new ShoutGridViewHolder(layoutInflater.inflate(R.layout.shout_item_grid, parent, false), picasso, context);
            case VIEW_TYPE_EMPTY_SHOUTS_ITEM:
                return new ShoutEmptyViewHolder(layoutInflater.inflate(R.layout.home_shouts_empty, parent, false));
            default:
                throw new RuntimeException("Unknown adapter view type");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        //noinspection unchecked
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
        } else if (item instanceof ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT_ITEM;
        } else if (item instanceof HomePresenter.ShoutsEmptyAdapterItem) {
            return VIEW_TYPE_EMPTY_SHOUTS_ITEM;
        } else {
            throw new RuntimeException("Unknown adapter view type");
        }
    }
}
