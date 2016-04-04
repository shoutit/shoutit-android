package com.shoutit.app.android.view.filter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.util.concurrent.Runnables;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxSeekBar;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.utils.rx.OnItemClickEvent;
import com.shoutit.app.android.utils.rx.RxUtils;
import com.shoutit.app.android.widget.CategorySpinnerAdapter;
import com.shoutit.app.android.widget.SortTypeSpinnerAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

        @Bind(R.id.filter_all_rb)
        RadioButton allButton;
        @Bind(R.id.filter_offers_rb)
        RadioButton offersButton;
        @Bind(R.id.filter_requests_rb)
        RadioButton requestButton;

        private CompositeSubscription subscription;
        private Handler handler;

        public ShoutTypeViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            handler = new Handler();
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.ShoutTypeAdapterItem item) {
            recycle();

            switch (item.getShoutTypeSelected()) {
                case Shout.TYPE_ALL:
                    allButton.setChecked(true);
                    break;
                case Shout.TYPE_OFFER:
                    offersButton.setChecked(true);
                    break;
                default:
                    requestButton.setChecked(true);
                    break;
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    subscription = new CompositeSubscription(

                            RxView.clicks(allButton)
                                    .subscribe(new Action1<Void>() {
                                        @Override
                                        public void call(Void aVoid) {
                                            item.onTypeSelected(Shout.TYPE_ALL);
                                        }
                                    }),

                            RxView.clicks(offersButton)
                                    .subscribe(new Action1<Void>() {
                                        @Override
                                        public void call(Void aVoid) {
                                            item.onTypeSelected(Shout.TYPE_OFFER);
                                        }
                                    }),

                            RxView.clicks(requestButton)
                                    .subscribe(new Action1<Void>() {
                                        @Override
                                        public void call(Void aVoid) {
                                            item.onTypeSelected(Shout.TYPE_REQUEST);
                                        }
                                    })
                    );
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
        private CompositeSubscription subscription;
        private final Handler handler;
        private Runnable runnable;

        public CategoryViewHolder(@Nonnull View itemView, Picasso picasso, Context context) {
            super(itemView);
            this.picasso = picasso;
            ButterKnife.bind(this, itemView);
            handler = new Handler();

            spinnerAdapter = new CategorySpinnerAdapter(
                    context, android.R.layout.simple_list_item_1);
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.CategoryAdapterItem item) {
            recycle();

            if (categorySpinner.getAdapter() == null) {
                categorySpinner.setAdapter(spinnerAdapter);
                spinnerAdapter.bindData(item.getCategories().values());
            }

            runnable = new Runnable() {
                @Override
                public void run() {
                    subscription = new CompositeSubscription(

                            RxUtils.spinnerItemClicks(categorySpinner)
                                    .distinctUntilChanged()
                                    .subscribe(new Action1<OnItemClickEvent>() {
                                        @Override
                                        public void call(OnItemClickEvent onItemClickEvent) {
                                            final int position = onItemClickEvent.position;
                                            if (position < 0) {
                                                return;
                                            }

                                            final Category selectedCategory = spinnerAdapter.getItem(position);
                                            Log.d("lol", "Selected category name:" + selectedCategory.getName() + " on position:" + position);
                                            item.onCategorySelected(selectedCategory);

                                            loadImage(selectedCategory.getIcon());
                                        }
                                    }),

                            item.getCategoryObservable()
                                    .first()
                                    .subscribe(new Action1<Category>() {
                                        @Override
                                        public void call(Category category) {
                                            final int itemPosition = spinnerAdapter.getItemPosition(category);
                                            categorySpinner.setSelection(itemPosition);
                                            loadImage(category.getIcon());
                                        }
                                    })
                    );
                }
            };
            handler.post(runnable);
        }

        public void loadImage(@Nullable String imageUrl) {
            picasso.load(imageUrl)
                    .fit()
                    .centerInside()
                    .into(categoryIcon);
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
            if (runnable != null) {
                handler.removeCallbacks(runnable);
                runnable = null;
            }
        }
    }

    public static class SortByViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.SortAdapterItem> {

        @Bind(R.id.filters_sort_types_spinner)
        Spinner sortTypesSpinner;

        private final SortTypeSpinnerAdapter spinnerAdapter;
        private Subscription subscription;
        private final Handler handler;

        public SortByViewHolder(@Nonnull View itemView, Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            handler = new Handler();

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

            handler.post(new Runnable() {
                @Override
                public void run() {
                    subscription = RxUtils.spinnerItemClicks(sortTypesSpinner)
                            .distinctUntilChanged()
                            .subscribe(new Action1<OnItemClickEvent>() {
                                @Override
                                public void call(OnItemClickEvent onItemClickEvent) {
                                    final int position = onItemClickEvent.position;
                                    if (position < 0) {
                                        return;
                                    }
                                    final SortType sortType = spinnerAdapter.getItem(position);
                                    item.onSortTypeSelected(sortType);
                                }
                            });
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
        private final Handler handler;

        public PriceViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            handler = new Handler();
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.PriceAdapterItem item) {
            recycle();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    subscription = new CompositeSubscription(

                            RxTextView.textChangeEvents(priceFromEt)
                                    .map(MoreFunctions1.mapTextChangeEventToString())
                                    .subscribe(item.getPriceFromObserver()),

                            RxTextView.textChangeEvents(priceToEt)
                                    .map(MoreFunctions1.mapTextChangeEventToString())
                                    .subscribe(item.getPriceFromObserver())
                    );
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

        private final Context context;
        private FiltersAdapterItems.DistanceAdapterItem item;
        private final Handler handler;
        private CompositeSubscription subscription;
        private final int maxProgress = 100;
        private final int valuesCount = 15;
        private final float singleValueRange = (float) maxProgress / (float) valuesCount;

        public DistanceViewHolder(@Nonnull View itemView, final Context context) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
            handler = new Handler();
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.DistanceAdapterItem item) {
            recycle();

            this.item = item;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    subscription = new CompositeSubscription(
                            RxSeekBar.userChanges(distanceSeekbar)
                                    .subscribe(new Action1<Integer>() {
                                        @Override
                                        public void call(Integer progress) {
                                            final int whichValue = (int) ((float) progress / singleValueRange);
                                            distanceTv.setText(getDisplayText(whichValue));
                                        }
                                    })

                    /*        item.getDistanceObservable()
                                    .subscribe(new Action1<Integer>() {
                                        @Override
                                        public void call(Integer whichValue) {
                                            distanceSeekbar.setProgress(whichValue * singleValueRange);
                                            distanceTv.setText(getDisplayText(whichValue));
                                        }
                                    })*/
                    );
                }
            });
        }

        public String getDisplayText(int whichValue) {
            item.onDistanceChanged(whichValue);

            switch (whichValue) {
                case 0:
                    return context.getString(R.string.filters_distance_1KM);
                case 1:
                    return context.getString(R.string.filters_distance_2KM);
                case 2:
                    return context.getString(R.string.filters_distance_3KM);
                case 3:
                    return context.getString(R.string.filters_distance_5KM);
                case 4:
                    return context.getString(R.string.filters_distance_7KM);
                case 5:
                    return context.getString(R.string.filters_distance_10KM);
                case 6:
                    return context.getString(R.string.filters_distance_15KM);
                case 7:
                    return context.getString(R.string.filters_distance_20KM);
                case 8:
                    return context.getString(R.string.filters_distance_30KM);
                case 9:
                    return context.getString(R.string.filters_distance_60KM);
                case 10:
                    return context.getString(R.string.filters_distance_100KM);
                case 11:
                    return context.getString(R.string.filters_distance_200KM);
                case 12:
                    return context.getString(R.string.filters_distance_300KM);
                case 13:
                    return context.getString(R.string.filters_distance_400KM);
                case 14:
                    return context.getString(R.string.filters_distance_500KM);
                case 15:
                    return context.getString(R.string.filters_distance_entire_country);
                default:
                    throw new RuntimeException("Unknown value: " + whichValue);
            }
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

    public static class FilterViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.FilterAdapterItem> {

        @Bind(R.id.filters_filter_name_tv)
        TextView nameTv;
        @Bind(R.id.filters_filter_values_tv)
        TextView valuesTv;
        @Bind(R.id.filter_filter_icon_iv)
        ImageView iconIv;

        private FiltersAdapterItems.FilterAdapterItem item;
        private final Context context;
        private final Handler handler;

        public FilterViewHolder(@Nonnull View itemView, Context context) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
            handler = new Handler();
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.FilterAdapterItem item) {
            this.item = item;
            nameTv.setText(item.getTitle());

            handler.post(new Runnable() {
                @Override
                public void run() {
                    item.getSelectedValuesMapObservable()
                            .subscribe(new Action1<ImmutableMultimap<String, CategoryFilter.FilterValue>>() {
                                @Override
                                public void call(ImmutableMultimap<String, CategoryFilter.FilterValue> selectedValuesMap) {
                                    final ImmutableCollection<CategoryFilter.FilterValue> filterValues = selectedValuesMap.get(item.getFilterSlug());
                                    if (filterValues != null) {
                                        final String selectedValues = item.getSelectedValues(filterValues);
                                        valuesTv.setText(selectedValues);
                                    }
                                }
                            });
                }
            });
        }

        @OnClick(R.id.filters_filter_root_view)
        public void onItemClicked() {
            item.onVisibilityChanged();
            iconIv.setImageDrawable(context.getResources().getDrawable(item.isVisible() ?
                    R.drawable.ic_expand_less : R.drawable.ic_expand_more));
        }
    }

    public static class FilterValueViewHolder extends ViewHolderManager.BaseViewHolder<FiltersAdapterItems.FilterValueAdapterItem> {
        @Bind(R.id.filters_value_tv)
        TextView valueTv;
        @Bind(R.id.filters_value_checkbox)
        CheckBox filtersValueCheckbox;

        private Subscription subscription;
        private final Handler handler;

        public FilterValueViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            handler = new Handler();
        }

        @Override
        public void bind(@Nonnull final FiltersAdapterItems.FilterValueAdapterItem item) {
            recycle();

            valueTv.setText(item.getFilterValue().getName());

            handler.post(new Runnable() {
                @Override
                public void run() {
                    subscription = new CompositeSubscription(

                            RxCompoundButton.checkedChanges(filtersValueCheckbox)
                                    .distinctUntilChanged()
                                    .subscribe(new Action1<Boolean>() {
                                        @Override
                                        public void call(Boolean isChecked) {
                                            item.toggleValueSelection(isChecked);
                                        }
                                    }),

                            item.getSelectedValuesMapObservable()
                                    .subscribe(new Action1<ImmutableMultimap<String, CategoryFilter.FilterValue>>() {
                                        @Override
                                        public void call(ImmutableMultimap<String, CategoryFilter.FilterValue> selectedValuesMap) {
                                            final ImmutableCollection<CategoryFilter.FilterValue> filterValues =
                                                    selectedValuesMap.get(item.getCategoryFilter().getSlug());

                                            for (CategoryFilter.FilterValue filterValue : filterValues) {
                                                if (filterValue.getSlug().equals(item.getFilterValue().getSlug())) {
                                                    filtersValueCheckbox.setChecked(true);
                                                    return;
                                                }
                                            }

                                            filtersValueCheckbox.setChecked(false);
                                        }
                                    })
                    );
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

}
