package com.shoutit.app.android.view.filter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;

public class FiltersAdapter extends BaseAdapter {
    private final int VIEW_TYPE_HEADER = 1;
    private final int VIEW_TYPE_SHOUT_TYPE = 2;
    private final int VIEW_TYPE_CATEGORY = 3;
    private final int VIEW_TYPE_PRICE = 4;
    private final int VIEW_TYPE_LOCATION = 5;
    private final int VIEW_TYPE_DISTANCE = 6;
    private final int VIEW_TYPE_FILTER = 7;
    private final int VIEW_TYPE_FILTER_VALUE = 8;

    @Nonnull
    private final Picasso picasso;

    @Inject
    public FiltersAdapter(@ForActivity Context context, @Nonnull Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new FilterViewHolders.HeaderViewHolder(layoutInflater.inflate(R.layout.filters_header_item, parent, false));
            case VIEW_TYPE_SHOUT_TYPE:
                return new FilterViewHolders.ShoutTypeViewHolder(layoutInflater.inflate(R.layout.filters_shout_type_item, parent, false));
            case VIEW_TYPE_CATEGORY:
                return new FilterViewHolders.CategoryViewHolder(layoutInflater.inflate(R.layout.filters_category_item, parent, false), picasso, context);
            case VIEW_TYPE_PRICE:
                return new FilterViewHolders.PriceViewHolder(layoutInflater.inflate(R.layout.filters_price_item, parent, false));
            case VIEW_TYPE_LOCATION:
                return new FilterViewHolders.LocationViewHolder(layoutInflater.inflate(R.layout.filters_location_item, parent, false));
            case VIEW_TYPE_DISTANCE:
                return new FilterViewHolders.DistanceViewHolder(layoutInflater.inflate(R.layout.filters_distance_item, parent, false));
            case VIEW_TYPE_FILTER:
                return new FilterViewHolders.FilterViewHolder(layoutInflater.inflate(R.layout.filters_filter_item, parent, false));
            case VIEW_TYPE_FILTER_VALUE:
                return new FilterViewHolders.FilterValueViewHolder(layoutInflater.inflate(R.layout.filters_filter_value_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type:" + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof FiltersAdapterItems.HeaderAdapterItem) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof FiltersAdapterItems.ShoutTypeAdapterItem) {
            return VIEW_TYPE_SHOUT_TYPE;
        } else if (item instanceof FiltersAdapterItems.CategoryAdapterItem) {
            return VIEW_TYPE_CATEGORY;
        } else if (item instanceof FiltersAdapterItems.PriceAdapterItem) {
            return VIEW_TYPE_PRICE;
        } else if (item instanceof FiltersAdapterItems.DistanceAdapterItem) {
            return VIEW_TYPE_DISTANCE;
        } else if (item instanceof FiltersAdapterItems.LocationAdapterItem) {
            return VIEW_TYPE_LOCATION;
        } else if (item instanceof FiltersAdapterItems.FilterAdapterItem) {
            return VIEW_TYPE_FILTER;
        } else if (item instanceof FiltersAdapterItems.FilterValueAdapterItem) {
            return VIEW_TYPE_FILTER_VALUE;
        } else {
            throw new RuntimeException("Unknown view type: " + item.getClass().getSimpleName());
        }
    }
}
