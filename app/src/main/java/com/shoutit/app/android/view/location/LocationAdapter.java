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

    class PlaceViewHolder extends ViewHolderManager.BaseViewHolder<LocationPresenter.PlaceAdapterItem> implements View.OnClickListener {

        @Bind(R.id.location_suggestion_item_tv)
        TextView placeTextView;

        private LocationPresenter.PlaceAdapterItem item;

        public PlaceViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull LocationPresenter.PlaceAdapterItem item) {
            this.item = item;
            placeTextView.setText(item.getFullText());
        }

        @Override
        public void onClick(View v) {
            item.locationSelected();
        }
    }

    class CurrentLocationViewHolder extends ViewHolderManager.BaseViewHolder<CurrentLocationAdapterItem> implements View.OnClickListener {

        @Bind(R.id.location_current_item_header_tv)
        TextView headerTextView;
        @Bind(R.id.location_current_item_place_tv)
        TextView placeTextView;

        private CurrentLocationAdapterItem item;

        public CurrentLocationViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull CurrentLocationAdapterItem item) {
            this.item = item;
            headerTextView.setText(item.getHeaderName());
            placeTextView.setText(context.getString(R.string.location_location,
                    item.getUserLocation().getCountry(), item.getUserLocation().getCity()));
        }

        @Override
        public void onClick(View v) {
            item.onLocationSelected();
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
        if (item instanceof CurrentLocationAdapterItem) {
            return VIEW_TYPE_CURRENT_LOCATION;
        } else if (item instanceof LocationPresenter.PlaceAdapterItem) {
            return VIEW_TYPE_PLACE;
        } else {
            throw new RuntimeException("Unknown view type");
        }
    }
}
