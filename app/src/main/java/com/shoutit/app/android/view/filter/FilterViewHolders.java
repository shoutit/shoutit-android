package com.shoutit.app.android.view.filter;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.widget.CategorySpinnerAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class FilterViewHolders {

    public static class HeaderViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.HeaderAdapterItem> {

        private FiltersAdapterItems.HeaderAdapterItem item;

        public HeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull FiltersAdapterItems.HeaderAdapterItem item) {
            this.item = item;
        }

        @OnClick(R.id.filter_reset_tv)
        public void onResetClick() {
            item.onResetClicked();
        }

        @OnClick(R.id.filter_done_tv)
        public void onDoneClicked() {
            item.onDoneClicked();
        }
    }

    public static class ShoutTypeViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.ShoutTypeAdapterItem> {

        private FiltersAdapterItems.ShoutTypeAdapterItem item;

        public ShoutTypeViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull FiltersAdapterItems.ShoutTypeAdapterItem item) {
            this.item = item;
        }

        @OnClick({R.id.filter_all_rb, R.id.filter_offers_rb, R.id.filter_requests_rb})
        public void onTypeSelected(View view) {
            switch (view.getId()) {
                case R.id.filter_all_rb:
                    item.onTypeSelected(Shout.TYPE_ALL);
                    break;
                case R.id.filter_offers_rb:
                    item.onTypeSelected(Shout.TYPE_OFFER);
                    break;
                case R.id.filter_requests_rb:
                    item.onTypeSelected(Shout.TYPE_REQUEST);
                    break;
            }
        }
    }

    public static class CategoryViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.CategoryAdapterItem> {

        @Bind(R.id.filters_category_icon)
        ImageView categoryIcon;
        @Bind(R.id.filters_category_spinner)
        Spinner categorySpinner;

        private final Picasso picasso;
        private final CategorySpinnerAdapter spinnerAdapter;
        private Subscription subscription;

        public CategoryViewHolder(@Nonnull View itemView, Picasso picasso, Context context) {
            super(itemView);
            this.picasso = picasso;
            ButterKnife.bind(this, itemView);

            spinnerAdapter = new CategorySpinnerAdapter(
                    context, android.R.layout.simple_list_item_1);
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.CategoryAdapterItem item) {
            final Category category = item.getCategory();

            if (categorySpinner.getAdapter() == null) {
                categorySpinner.setAdapter(spinnerAdapter);
                spinnerAdapter.bindData(item.getCategories());
            }

            subscription = RxAdapterView.itemSelections(categorySpinner)
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer position) {
                            final Category selectedCategory = spinnerAdapter.getItem(position);
                            item.onCategorySelected(selectedCategory);

                            picasso.load(category.getIcon())
                                    .fit()
                                    .centerInside()
                                    .into(categoryIcon);
                        }
                    });
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

    public static class PriceViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.PriceAdapterItem> {
        @Bind(R.id.filters_price_from_et)
        EditText priceFromEt;
        @Bind(R.id.filters_price_to_et)
        EditText priceToEt;

        private CompositeSubscription subscription;

        public PriceViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull FiltersAdapterItems.PriceAdapterItem item) {

            subscription = new CompositeSubscription(

                    RxTextView.textChangeEvents(priceFromEt)
                            .map(MoreFunctions1.mapTextChangeEventToString())
                            .subscribe(item.getPriceFromObserver()),

                    RxTextView.textChangeEvents(priceToEt)
                            .map(MoreFunctions1.mapTextChangeEventToString())
                            .subscribe(item.getPriceFromObserver())
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

    public static class LocationViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.LocationAdapterItem> {

        @Bind(R.id.filters_flag_iv)
        ImageView flagIv;
        @Bind(R.id.filters_location_tv)
        TextView locationTv;

        private final Context context;
        private final Picasso picasso;
        private final Target flagTarget;
        private FiltersAdapterItems.LocationAdapterItem item;

        public LocationViewHolder(@Nonnull View itemView, Context context, Picasso picasso) {
            super(itemView);
            this.context = context;
            this.picasso = picasso;
            ButterKnife.bind(this, itemView);

            flagTarget = PicassoHelper.getRoundedBitmapTarget(context, flagIv);
        }

        @Override
        public void bind(@Nonnull FiltersAdapterItems.LocationAdapterItem item) {
            this.item = item;
            final UserLocation userLocation = item.getUserLocation();

            locationTv.setText(context.getString(R.string.edit_profile_country,
                    Strings.nullToEmpty(userLocation.getCity()),
                    Strings.nullToEmpty(userLocation.getCountry())));

            final Optional<Integer> countryResId = ResourcesHelper
                    .getCountryResId(context, userLocation);

            if (countryResId.isPresent()) {
                picasso.load(countryResId.get())
                        .into(flagTarget);
            } else {
                picasso.load((String) null)
                        .into(flagTarget);
            }
        }

        @OnClick(R.id.filters_change_location_tv)
        public void onChangeLocationClick() {
            item.onLocationChangeClicked();
        }
    }

    public static class DistanceViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.DistanceAdapterItem> {

        public DistanceViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull FiltersAdapterItems.DistanceAdapterItem item) {

        }
    }

    public static class FilterViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.FilterAdapterItem> {

        public FilterViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull FiltersAdapterItems.FilterAdapterItem item) {

        }
    }

    public static class FilterValueViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.FilterValueAdapterItem> {

        public FilterValueViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull FiltersAdapterItems.FilterValueAdapterItem item) {

        }
    }

}
