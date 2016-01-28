package com.shoutit.app.android.view.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.detector.ChangesDetector;
import com.appunite.detector.SimpleDetector;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.functions.Action1;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        Action1<List<BaseAdapterItem>>, ChangesDetector.ChangesAdapter {

    private static final int VIEW_TYPE_DISCOVER_CONTAINER = 0;
    private static final int VIEW_TYPE_SHOUT_HEADER = 1;
    private static final int VIEW_TYPE_SHOUT_ITEM = 2;

    @Nonnull
    private final ChangesDetector<BaseAdapterItem, BaseAdapterItem> changesDetector;
    @Nonnull
    private final LayoutInflater layoutInflater;
    @Nonnull
    private List<BaseAdapterItem> items = ImmutableList.of();

    @Inject
    public HomeAdapter(@ForActivity @Nonnull Context context) {
        changesDetector = new ChangesDetector<>(new SimpleDetector<BaseAdapterItem>());
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void call(List<BaseAdapterItem> items) {
        this.items = items;
        changesDetector.newData(this, items, false);
    }

    public class DiscoverContainerViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.DiscoverContainerAdapterItem> {

        public DiscoverContainerViewHolder(@Nonnull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverContainerAdapterItem item) {

        }
    }

    public class ShoutHeaderViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.ShoutHeaderAdapterItem> {

        public ShoutHeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.ShoutHeaderAdapterItem item) {

        }
    }

    public class ShoutViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.ShoutAdapterItem> {

        public ShoutViewHolder(@Nonnull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.ShoutAdapterItem item) {

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return items.size();
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
