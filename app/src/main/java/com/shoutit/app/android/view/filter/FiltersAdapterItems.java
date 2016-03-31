package com.shoutit.app.android.view.filter;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observer;

public class FiltersAdapterItems {

    public static class FilterValueAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final CategoryFilter.FilterValue filterValue;
        @Nonnull
        private final Observer<Object> selectedFilterValuesObserver;
        private boolean isSelected = false;

        public FilterValueAdapterItem(@Nonnull CategoryFilter.FilterValue filterValue,
                                      @Nonnull Observer<Object> selectedFilterValuesObserver) {
            this.filterValue = filterValue;
            this.selectedFilterValuesObserver = selectedFilterValuesObserver;
        }

        public void toggleValueSelection() {
            isSelected = !isSelected;
            selectedFilterValuesObserver.onNext(null);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof FilterValueAdapterItem &&
                    filterValue.getSlug().equals(((FilterValueAdapterItem) item).filterValue.getSlug());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof FilterValueAdapterItem && item.equals(this);
        }

        @Nonnull
        public CategoryFilter.FilterValue getFilterValue() {
            return filterValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterValueAdapterItem)) return false;
            final FilterValueAdapterItem that = (FilterValueAdapterItem) o;
            return isSelected == that.isSelected &&
                    Objects.equal(filterValue, that.filterValue);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(filterValue, isSelected);
        }
    }

    public static class FilterAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final CategoryFilter categoryFilter;
        @Nonnull
        private final List<CategoryFilter.FilterValue> selectedFilters;
        @Nonnull
        private final Observer<Object> filterVisibilityChangedObserver;
        private boolean isVisible = false;
        private List<CategoryFilter.FilterValue> selectedValues = ImmutableList.of();

        public FilterAdapterItem(@Nonnull CategoryFilter categoryFilter,
                                 @Nonnull List<CategoryFilter.FilterValue> selectedFilters,
                                 @Nonnull Observer<Object> filterVisibilityChangedObserver) {
            this.categoryFilter = categoryFilter;
            this.selectedFilters = selectedFilters;
            this.filterVisibilityChangedObserver = filterVisibilityChangedObserver;
        }

        public void onVisibilityChanged() {
            isVisible = !isVisible;
            filterVisibilityChangedObserver.onNext(null);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof FilterAdapterItem &&
                    categoryFilter.getSlug().equals(((FilterAdapterItem) item).categoryFilter.getSlug());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof FilterAdapterItem && this.equals(item);
        }

        public void setSelectedValues(ImmutableList<CategoryFilter.FilterValue> selectedValues) {
            this.selectedValues = selectedValues;
        }

        @Nonnull
        public String getSelectedValues() {
            String separator = "";
            final StringBuilder builder = new StringBuilder();
            for (CategoryFilter.FilterValue value : selectedValues) {
                builder.append(separator).append(value.getName());
                separator = ", ";
            }

            return builder.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterAdapterItem)) return false;
            final FilterAdapterItem that = (FilterAdapterItem) o;
            return isVisible == that.isVisible &&
                    Objects.equal(categoryFilter, that.categoryFilter) &&
                    Objects.equal(selectedFilters, that.selectedFilters) &&
                    Objects.equal(selectedValues, that.selectedValues);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(categoryFilter, selectedFilters, isVisible, selectedValues);
        }

        public boolean isVisible() {
            return isVisible;
        }
    }

    public static class HeaderAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Observer<Object> resetObserver;
        @Nonnull
        private final Observer<Object> doneObserver;

        public HeaderAdapterItem(@Nonnull Observer<Object> resetObserver,
                                 @Nonnull Observer<Object> doneObserver) {
            this.resetObserver = resetObserver;
            this.doneObserver = doneObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof HeaderAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return true;
        }

        public void onDoneClicked() {
            doneObserver.onNext(null);
        }

        public void onResetClicked() {
            resetObserver.onNext(null);
        }
    }

    public static class ShoutTypeAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Observer<String> shoutTypeObserver;

        public ShoutTypeAdapterItem(@Nonnull Observer<String> shoutTypeObserver) {
            this.shoutTypeObserver = shoutTypeObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ShoutTypeAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }

        public void onTypeSelected(@Nonnull String type) {
            shoutTypeObserver.onNext(type);
        }
    }

    public static class CategoryAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Category category;
        @Nonnull
        private final List<Category> categories;
        @Nonnull
        private final Observer<Category> categorySelectedObserver;

        public CategoryAdapterItem(@Nonnull Category category,
                                   @Nonnull List<Category> categories,
                                   @Nonnull Observer<Category> categorySelectedObserver) {
            this.category = category;
            this.categories = categories;
            this.categorySelectedObserver = categorySelectedObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof CategoryAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof CategoryAdapterItem
                    && category.equals(((CategoryAdapterItem) item).category);
        }

        @Nonnull
        public Category getCategory() {
            return category;
        }

        @Nonnull
        public List<Category> getCategories() {
            return categories;
        }

        public void onCategorySelected(Category category) {
            categorySelectedObserver.onNext(category);
        }
    }

    public static class PriceAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Observer<String> priceFromObserver;
        @Nonnull
        private final Observer<String> priceToObserver;

        public PriceAdapterItem(@Nonnull Observer<String> priceFromObserver,
                                @Nonnull Observer<String> priceToObserver) {
            this.priceFromObserver = priceFromObserver;
            this.priceToObserver = priceToObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof PriceAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return true;
        }

        @Nonnull
        public Observer<String> getPriceFromObserver() {
            return priceFromObserver;
        }

        @Nonnull
        public Observer<String> getPriceToObserver() {
            return priceToObserver;
        }
    }

    public static class LocationAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final UserLocation userLocation;
        @Nonnull
        private final Observer<Object> locationChangeClickObserver;

        public LocationAdapterItem(@Nonnull UserLocation userLocation,
                                   @Nonnull Observer<Object> locationChangeClickObserver) {
            this.userLocation = userLocation;
            this.locationChangeClickObserver = locationChangeClickObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof LocationAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof LocationAdapterItem &&
                    userLocation.equals(((LocationAdapterItem) item).userLocation);
        }

        @Nonnull
        public UserLocation getUserLocation() {
            return userLocation;
        }

        public void onLocationChangeClicked() {
            locationChangeClickObserver.onNext(null);
        }
    }

    public static class DistanceAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Observer<Integer> distanceChangedObserver;

        public DistanceAdapterItem(@Nonnull Observer<Integer> distanceChangedObserver) {
            this.distanceChangedObserver = distanceChangedObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DistanceAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return true;
        }
    }

    public static class SortAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final SortType sortType;
        @Nonnull
        private final Observer<Object> sortTypeChangeClickedObserver;

        public SortAdapterItem(@Nonnull SortType sortType,
                               @Nonnull Observer<Object> sortTypeChangeClickedObserver) {
            this.sortType = sortType;
            this.sortTypeChangeClickedObserver = sortTypeChangeClickedObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof SortAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SortAdapterItem &&
                    sortType.equals(((SortAdapterItem) item).sortType);
        }
    }
}
