package com.shoutit.app.android.view.filter;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.functions.BothParams;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMultimap;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;

public class FiltersAdapterItems {

    public static class FilterValueAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final CategoryFilter categoryFilter;
        @Nonnull
        private final CategoryFilter.FilterValue filterValue;
        @Nonnull
        private final Observer<FiltersPresenter.AdapterFilterValue> selectedFilterValuesObserver;
        @Nonnull
        private final Observable<ImmutableMultimap<String, CategoryFilter.FilterValue>> selectedValuesMapObservable;

        public FilterValueAdapterItem(@Nonnull CategoryFilter categoryFilter,
                                      @Nonnull CategoryFilter.FilterValue filterValue,
                                      @Nonnull Observer<FiltersPresenter.AdapterFilterValue> selectedFilterValuesObserver,
                                      @Nonnull Observable<ImmutableMultimap<String, CategoryFilter.FilterValue>> selectedValuesMapObservable) {
            this.categoryFilter = categoryFilter;
            this.filterValue = filterValue;
            this.selectedFilterValuesObserver = selectedFilterValuesObserver;
            this.selectedValuesMapObservable = selectedValuesMapObservable;
        }

        public void toggleValueSelection() {
            selectedFilterValuesObserver.onNext(
                    new FiltersPresenter.AdapterFilterValue(categoryFilter, filterValue));
        }

        @Nonnull
        public Observable<ImmutableMultimap<String, CategoryFilter.FilterValue>> getSelectedValuesMapObservable() {
            return selectedValuesMapObservable;
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

        @Nonnull
        public CategoryFilter getCategoryFilter() {
            return categoryFilter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterValueAdapterItem)) return false;
            final FilterValueAdapterItem that = (FilterValueAdapterItem) o;
            return Objects.equal(filterValue, that.filterValue);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(filterValue);
        }
    }

    public static class FilterAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final CategoryFilter categoryFilter;
        @Nonnull
        private final Observer<BothParams<String, Boolean>> filterVisibilityChangedObserver;
        @Nonnull
        private final Observable<ImmutableMultimap<String, CategoryFilter.FilterValue>> selectedValuesMapObservable;
        private boolean hasVisibleValues = false;

        public FilterAdapterItem(@Nonnull CategoryFilter categoryFilter,
                                 @Nonnull Observer<BothParams<String, Boolean>> filterVisibilityChangedObserver,
                                 @Nonnull Observable<ImmutableMultimap<String, CategoryFilter.FilterValue>> selectedValuesMapObservable,
                                 boolean hasVisibleValues) {
            this.categoryFilter = categoryFilter;
            this.filterVisibilityChangedObserver = filterVisibilityChangedObserver;
            this.selectedValuesMapObservable = selectedValuesMapObservable;
            this.hasVisibleValues = hasVisibleValues;
        }

        public void onVisibilityChanged() {
            filterVisibilityChangedObserver.onNext(
                    new BothParams<>(categoryFilter.getSlug(), !hasVisibleValues));
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

        public String getTitle() {
            return categoryFilter.getName();
        }

        @Nonnull
        public Observable<ImmutableMultimap<String, CategoryFilter.FilterValue>> getSelectedValuesMapObservable() {
            return selectedValuesMapObservable;
        }

        @Nonnull
        public String getFilterSlug() {
            return categoryFilter.getSlug();
        }

        @Nonnull
        public String getSelectedValues(@Nonnull Collection<CategoryFilter.FilterValue> selectedValues) {
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
            return hasVisibleValues == that.hasVisibleValues &&
                    Objects.equal(categoryFilter, that.categoryFilter);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(categoryFilter, hasVisibleValues);
        }

        public boolean isHasVisibleValues() {
            return hasVisibleValues;
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
        @Nonnull
        private final String shoutTypeSelected;

        public ShoutTypeAdapterItem(@Nonnull Observer<String> shoutTypeObserver,
                                    @Nonnull String shoutTypeSelected) {
            this.shoutTypeObserver = shoutTypeObserver;
            this.shoutTypeSelected = shoutTypeSelected;
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

        @Nonnull
        public String getShoutTypeSelected() {
            return shoutTypeSelected;
        }
    }

    public static class CategoryAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Category category;
        @Nonnull
        private final Map<String, Category> categories;
        @Nonnull
        private final Observer<String> categorySelectedObserver;
        @Nonnull
        private final Observable<Category> categoryObservable;
        private final boolean shouldBlockCategories;

        public CategoryAdapterItem(@Nonnull Category category,
                                   @Nonnull Map<String, Category> categories,
                                   @Nonnull Observer<String> categorySelectedObserver,
                                   @Nonnull Observable<Category> categoryObservable,
                                   boolean shouldBlockCategories) {
            this.category = category;
            this.categories = categories;
            this.categorySelectedObserver = categorySelectedObserver;
            this.categoryObservable = categoryObservable;
            this.shouldBlockCategories = shouldBlockCategories;
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
        public Map<String, Category> getCategories() {
            return categories;
        }

        public void onCategorySelected(Category category) {
            categorySelectedObserver.onNext(category.getSlug());
        }

        @Nonnull
        public Observable<Category> getCategoryObservable() {
            return categoryObservable;
        }

        public boolean shouldBlockCategories() {
            return shouldBlockCategories;
        }
    }

    public static class PriceAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Observer<String> minPriceObserver;
        @Nonnull
        private final Observer<String> maxPriceObserver;
        @Nonnull
        private final Observable<Object> resetClickedObserver;

        public PriceAdapterItem(@Nonnull Observer<String> minPriceObserver,
                                @Nonnull Observer<String> maxPriceObserver,
                                @Nonnull Observable<Object> resetClickedObserver) {
            this.minPriceObserver = minPriceObserver;
            this.maxPriceObserver = maxPriceObserver;
            this.resetClickedObserver = resetClickedObserver;
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
        public Observer<String> getMinPriceObserver() {
            return minPriceObserver;
        }

        @Nonnull
        public Observer<String> getMaxPriceObserver() {
            return maxPriceObserver;
        }

        @Nonnull
        public Observable<Object> getResetClickedObservable() {
            return resetClickedObserver;
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
        private final BehaviorSubject<Integer> distanceChangedSubject;
        @Nonnull
        private final Observable<Object> resetClickedObserver;

        public DistanceAdapterItem(@Nonnull BehaviorSubject<Integer> distanceChangedSubject,
                                   @Nonnull Observable<Object> resetClickedObserver) {
            this.distanceChangedSubject = distanceChangedSubject;
            this.resetClickedObserver = resetClickedObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DistanceAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return true;
        }

        public void onDistanceChanged(int distance) {
            distanceChangedSubject.onNext(distance);
        }

        @Nonnull
        public Observable<Integer> getDistanceObservable() {
            return distanceChangedSubject;
        }

        @Nonnull
        public Observable<Object> getResetClickedObserver() {
            return resetClickedObserver;
        }
    }

    public static class SortAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final SortType sortType;
        @Nonnull
        private final List<SortType> sortTypes;
        @Nonnull
        private final Observer<SortType> sortTypeChangeObserver;
        @Nonnull
        private final Observable<SortType> sortTypeObservable;
        @Nonnull
        private final Observable<Object> resetClickedObserver;

        public SortAdapterItem(@Nonnull SortType sortType,
                               @Nonnull List<SortType> sortTypes,
                               @Nonnull Observer<SortType> sortTypeChangeObserver,
                               @Nonnull Observable<SortType> sortTypeObservable,
                               @Nonnull Observable<Object> resetClickedObserver) {
            this.sortType = sortType;
            this.sortTypes = sortTypes;
            this.sortTypeChangeObserver = sortTypeChangeObserver;
            this.sortTypeObservable = sortTypeObservable;
            this.resetClickedObserver = resetClickedObserver;
        }

        @Nonnull
        public Observable<SortType> getSortTypeObservable() {
            return sortTypeObservable;
        }

        @Nonnull
        public Observable<Object> getResetClickedObserver() {
            return resetClickedObserver;
        }

        @Nonnull
        public List<SortType> getSortTypes() {
            return sortTypes;
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

        public void onSortTypeSelected(SortType sortType) {
            sortTypeChangeObserver.onNext(sortType);
        }
    }
}
