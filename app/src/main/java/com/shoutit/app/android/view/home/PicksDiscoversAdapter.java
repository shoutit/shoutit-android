package com.shoutit.app.android.view.home;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

public class PicksDiscoversAdapter extends BaseAdapter {

    @Nonnull
    private final Picasso picasso;

    @Inject
    public PicksDiscoversAdapter(@Nonnull Picasso picasso,
                                 @Nonnull @ForActivity Context context) {
        super(context);
        this.picasso = picasso;
    }

    class DiscoverItemViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.DiscoverAdapterItem> implements View.OnClickListener {
        @Bind(R.id.picks_discover_item_iv)
        ImageView discoverIv;
        @Bind(R.id.picks_discover_item_name_tv)
        TextView discoverTv;
        private HomePresenter.DiscoverAdapterItem item;

        public DiscoverItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverAdapterItem item) {
            this.item = item;
            final DiscoverChild discover = item.getDiscover();
            discoverTv.setText(discover.getTitle());

            picasso.load(Strings.emptyToNull(discover.getImage()))
                    .placeholder(R.drawable.pattern_placeholder)
                    .fit()
                    .centerCrop()
                    .into(discoverIv);
        }

        @Override
        public void onClick(View v) {
            item.onDiscoverSelected();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DiscoverItemViewHolder(layoutInflater.inflate(R.layout.home_discover_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }
}
