package com.shoutit.app.android.view.filter;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxRadioGroup;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.widget.CategorySpinnerAdapter;
import com.shoutit.app.android.widget.SortTypeSpinnerAdapter;
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

        @Bind(R.id.filters_radio_group)
        RadioGroup radioGroup;

        private Subscription subscription;

        public ShoutTypeViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.ShoutTypeAdapterItem item) {
            recycle();

            subscription = RxRadioGroup.checkedChanges(radioGroup)
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer viewId) {
                            switch (viewId) {
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
            recycle();

            final Category category = item.getCategory();

            if (categorySpinner.getAdapter() == null) {
                categorySpinner.setAdapter(spinnerAdapter);
                spinnerAdapter.bindData(item.getCategories());
            }

            subscription = RxAdapterView.itemSelections(categorySpinner)
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer position) {
                            if (position < 0) {
                                return;
                            }
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

    public static class SortTypeViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.SortAdapterItem> {

        @Bind(R.id.filters_sort_types_spinner)
        Spinner sortTypesSpinner;

        private final SortTypeSpinnerAdapter spinnerAdapter;
        private Subscription subscription;

        public SortTypeViewHolder(@Nonnull View itemView, Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            spinnerAdapter = new SortTypeSpinnerAdapter(
                    context, android.R.layout.simple_list_item_1);
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.SortAdapterItem item) {
            recycle();

            if (sortTypesSpinner.getAdapter() == null) {
                sortTypesSpinner.setAdapter(spinnerAdapter);
                spinnerAdapter.bindData(item.getSortTypes());
            }

            subscription = RxAdapterView.itemSelections(sortTypesSpinner)
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer position) {
                            if (position < 0) {
                                return;
                            }
                            final SortType sortType = spinnerAdapter.getItem(position);
                            item.onSortTypeSelected(sortType);
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
            recycle();

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

        @Bind(R.id.filters_distance_tv)
        TextView distanceTv;
        @Bind(R.id.filters_distance_seekbar)
        SeekBar distanceSeekbar;

        public DistanceViewHolder(@Nonnull View itemView, final Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            distanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                final int maxProgress = 100;
                final int valuesCount = 16;
                final int singleValueRange = maxProgress / valuesCount;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    distanceTv.setText(getDisplayText(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public String getDisplayText(int progress) {
                    switch (progress / singleValueRange) {
                        case 0:
                            return context.getString(R.string.filters_distance_0KM);
                        case 1:
                            return context.getString(R.string.filters_distance_1KM);
                        case 2:
                            return context.getString(R.string.filters_distance_2KM);
                        case 3:
                            return context.getString(R.string.filters_distance_3KM);
                        case 4:
                            return context.getString(R.string.filters_distance_5KM);
                        case 5:
                            return context.getString(R.string.filters_distance_7KM);
                        case 6:
                            return context.getString(R.string.filters_distance_10KM);
                        case 7:
                            return context.getString(R.string.filters_distance_15KM);
                        case 8:
                            return context.getString(R.string.filters_distance_20KM);
                        case 9:
                            return context.getString(R.string.filters_distance_30KM);
                        case 10:
                            return context.getString(R.string.filters_distance_60KM);
                        case 11:
                            return context.getString(R.string.filters_distance_100KM);
                        case 12:
                            return context.getString(R.string.filters_distance_200KM);
                        case 13:
                            return context.getString(R.string.filters_distance_300KM);
                        case 14:
                            return context.getString(R.string.filters_distance_400KM);
                        case 15:
                            return context.getString(R.string.filters_distance_500KM);
                        case 16:
                            return context.getString(R.string.filters_distance_entire_country);
                        default:
                            throw new RuntimeException("Unknown progress: " + progress);
                    }
                }
            });
        }

        @Override
        public void bind(@Nonnull FiltersAdapterItems.DistanceAdapterItem item) {
        }
    }

    public static class FilterViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.FilterAdapterItem> {

        @Bind(R.id.filters_filter_name_tv)
        TextView nameTv;
        @Bind(R.id.filters_filter_values_tv)
        TextView valuesTv;
        @Bind(R.id.filter_filter_icon_iv)
        ImageView iconIv;

        private FiltersAdapterItems.FilterAdapterItem item;
        private final Context context;

        public FilterViewHolder(@Nonnull View itemView, Context context) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull FiltersAdapterItems.FilterAdapterItem item) {
            this.item = item;
            nameTv.setText(item.getTitle());
            valuesTv.setText(item.getSelectedValues());
            iconIv.setImageDrawable(context.getResources().getDrawable(item.isVisible() ?
                    R.drawable.ic_expand_less : R.drawable.ic_expand_more));
        }

        @OnClick(R.id.filters_filter_root_view)
        public void onItemClicked() {
            item.onVisibilityChanged();
        }
    }

    public static class FilterValueViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.FilterValueAdapterItem> {
        @Bind(R.id.filters_value_tv)
        TextView valueTv;
        @Bind(R.id.filters_value_checkbox)
        CheckBox filtersValueCheckbox;

        public FilterValueViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.FilterValueAdapterItem item) {
            valueTv.setText(item.getFilterValue().getName());

            filtersValueCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.toggleValueSelection();
                }
            });
        }
    }

}
