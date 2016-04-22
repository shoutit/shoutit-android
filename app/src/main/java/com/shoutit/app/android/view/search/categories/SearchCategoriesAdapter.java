package com.shoutit.app.android.view.search.categories;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.viewholders.NoDataViewHolder;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchCategoriesAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_CATEGORY = 2;

    @Nonnull
    private final Picasso picasso;

    @Inject
    public SearchCategoriesAdapter(@ForActivity @Nonnull Context context,
                                   @NonNull @Named("NoAmazonTransformer") Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    public class CategoryViewHolder extends ViewHolderManager.BaseViewHolder<SearchCategoriesPresenter.CategoryAdapterItem> implements View.OnClickListener {
        @Bind(R.id.search_category_item_icon_iv)
        ImageView iconIv;
        @Bind(R.id.search_category_item_text_tv)
        TextView titleTv;

        private SearchCategoriesPresenter.CategoryAdapterItem item;

        public CategoryViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull SearchCategoriesPresenter.CategoryAdapterItem item) {
            this.item = item;
            final Category category = item.getCategory();

            titleTv.setText(category.getName());

            picasso.load(category.getIcon())
                    .fit()
                    .centerInside()
                    .into(iconIv);
        }

        @Override
        public void onClick(View v) {
            item.onCategorySelected();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new NoDataViewHolder(layoutInflater.inflate(R.layout.search_category_header_item, parent, false));
            case VIEW_TYPE_CATEGORY:
                return new CategoryViewHolder(layoutInflater.inflate(R.layout.search_category_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type for " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof NoDataAdapterItem) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof SearchCategoriesPresenter.CategoryAdapterItem) {
            return VIEW_TYPE_CATEGORY;
        } else {
            throw new RuntimeException("Unknown view type for " + item.getClass().getSimpleName());
        }
    }
}
