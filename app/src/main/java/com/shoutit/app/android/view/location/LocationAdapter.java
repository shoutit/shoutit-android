package com.shoutit.app.android.view.location;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LocationAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_CURRENT_LOCATION = 1;
    private static final int VIEW_TYPE_PLACE = 2;

    @Inject
    public LocationAdapter(@ForActivity Context context) {
        super(context);
    }

    class PlaceViewHolder extends ViewHolderManager.BaseViewHolder<LocationPresenter.PlaceAdapterItem> {

        @Bind(R.id.location_suggestion_item_tv)
        TextView placeTextView;

        public PlaceViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull LocationPresenter.PlaceAdapterItem item) {
            placeTextView.setText(item.getFullText());
        }
    }

    class CurrentLocationViewHolder extends ViewHolderManager.BaseViewHolder<LocationPresenter.CurrentLocationAdapterItem> {

        @Bind(R.id.location_current_item_header_tv)
        TextView headerTextView;
        @Bind(R.id.location_current_item_place_tv)
        TextView placeTextView;

        public CurrentLocationViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull LocationPresenter.CurrentLocationAdapterItem item) {
            headerTextView.setText(item.getHeaderName());
            placeTextView.setText(context.getString(R.string.location_location,
                    item.getUserLocation().getCountry(), item.getUserLocation().getCity()));
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CURRENT_LOCATION:
                return new CurrentLocationViewHolder(
                        layoutInflater.inflate(R.layout.location_current_location_item, parent, false));
            case VIEW_TYPE_PLACE:
                return new PlaceViewHolder(
                        layoutInflater.inflate(R.layout.location_suggestion_item, parent, false));
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
        if (item instanceof LocationPresenter.CurrentLocationAdapterItem) {
            return VIEW_TYPE_CURRENT_LOCATION;
        } else if (item instanceof LocationPresenter.PlaceAdapterItem) {
            return VIEW_TYPE_PLACE;
        } else {
            throw new RuntimeException("Unknown view type");
        }
    }
}
