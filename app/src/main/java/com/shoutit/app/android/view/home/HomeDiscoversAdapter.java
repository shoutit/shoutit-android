package com.shoutit.app.android.view.home;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Strings;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeDiscoversAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_DISCOVER_ITEM = 1;
    private static final int VIEW_TYPE_SHOW_MORE = 2;

    @Nonnull
    private final Picasso picasso;

    @Inject
    public HomeDiscoversAdapter(@Nonnull Picasso picasso,
                                @Nonnull @ForActivity Context context) {
        super(context);
        this.picasso = picasso;
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
            final DiscoverChild discover = item.getDiscover();
            cardTitleTextView.setText(discover.getTitle());

            picasso.load(Strings.emptyToNull(discover.getImage()))
                    .placeholder(R.drawable.pattern_placeholder)
                    .fit()
                    .centerCrop()
                    .into(cardImageView);
        }
    }

    class ShowAllItemsViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.DiscoverShowAllAdapterItem> {

        private HomePresenter.DiscoverShowAllAdapterItem item;

        public ShowAllItemsViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverShowAllAdapterItem item) {
            this.item = item;
        }

        @OnClick(R.id.home_see_all_tv)
        public void onSeeAllClick() {
            item.getShowAllDiscoversObserver().onNext(true);
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SHOW_MORE:
                return new ShowAllItemsViewHolder(layoutInflater.inflate(R.layout.home_see_all_item, parent, false));
            case VIEW_TYPE_DISCOVER_ITEM:
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
        if (item instanceof HomePresenter.DiscoverShowAllAdapterItem) {
            return VIEW_TYPE_SHOW_MORE;
        } else if (item instanceof HomePresenter.DiscoverAdapterItem) {
            return VIEW_TYPE_DISCOVER_ITEM;
        } else {
            throw new RuntimeException("Unknown adapter view type");
        }
    }
}
