package com.shoutit.app.android.view.filter;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.utils.MoreFunctions1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class FiltersPresenter {

    private final PublishSubject<BothParams<String, Boolean>> filterVisibilityChanged = PublishSubject.create();
    private final PublishSubject<FilterValueAdapterItem> filterValueSelected = PublishSubject.create();


    public FiltersPresenter(@Nonnull CategoriesDao categoriesDao,
                            @Nonnull @UiScheduler Scheduler uiScheduler) {

        final Observable<ResponseOrError<List<Category>>> categoriesObservable = categoriesDao
                .categoriesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<List<Category>>>behaviorRefCount());

        final Observable<HashMap<String, Category>> successCategoriesObservable = categoriesObservable
                .compose(ResponseOrError.<List<Category>>onlySuccess())
                .map(new Func1<List<Category>, HashMap<String, Category>>() {
                    @Override
                    public HashMap<String, Category> call(List<Category> categories) {
                        final HashMap<String, Category> linkedHashMap = new LinkedHashMap<>();
                        for (Category category : categories) {
                            linkedHashMap.put(category.getSlug(), category);
                        }

                        return linkedHashMap;
                    }
                })
                .compose(ObservableExtensions.<HashMap<String, Category>>behaviorRefCount());

        final PublishSubject<String> categorySelected = PublishSubject.create();

        final Observable<Category> selectedCategory = categorySelected
                .withLatestFrom(successCategoriesObservable,
                        new Func2<String, HashMap<String, Category>, Category>() {
                            @Override
                            public Category call(String categorySlug, HashMap<String, Category> categoriesMap) {
                                return categoriesMap.get(categorySlug);
                            }
                        })
                .compose(ObservableExtensions.<Category>behaviorRefCount());

        final Observable<HashMap<String, BaseAdapterItem>> filterItems = selectedCategory
                .map(new Func1<Category, List<CategoryFilter>>() {
                    @Override
                    public List<CategoryFilter> call(Category category) {
                        return category.getFilters();
                    }
                })
                .filter(MoreFunctions1.<CategoryFilter>listNotEmpty())
                .map(new Func1<List<CategoryFilter>, HashMap<String, BaseAdapterItem>>() {
                    @Override
                    public HashMap<String, BaseAdapterItem> call(List<CategoryFilter> categoryFilters) {
                        final HashMap<String, BaseAdapterItem> itemsMap = new LinkedHashMap<>();
                        for (CategoryFilter filter : categoryFilters) {
                            itemsMap.put(filter.getSlug(), new FilterAdapterItem(filter, ImmutableList.<String>of()));

                            for (CategoryFilter.FilterValue value : filter.getValues()) {
                                itemsMap.put(value.getSlug(), new FilterValueAdapterItem(value, false, filter.getSlug()));
                            }
                        }

                        return itemsMap;
                    }
                })
                .compose(ObservableExtensions.<HashMap<String,BaseAdapterItem>>behaviorRefCount());

        filterValueSelected
                .withLatestFrom(filterItems, new Func2<FilterValueAdapterItem, HashMap<String,BaseAdapterItem>, HashMap<String,BaseAdapterItem>>() {
                    @Override
                    public HashMap<String, BaseAdapterItem> call(FilterValueAdapterItem selectedValueItem,
                                                                 HashMap<String, BaseAdapterItem> filtersAndValuesMap) {
                        final HashMap<String, BaseAdapterItem> filtersAndValues = new LinkedHashMap<>(filtersAndValuesMap);

                        final String selectedValueSlug = selectedValueItem.filterValue.getSlug();
                        filtersAndValues.put(selectedValueSlug, selectedValueItem.withToggledSelection());

                        final FilterAdapterItem filterItemToUpdate = (FilterAdapterItem) filtersAndValues.get(selectedValueItem.categorySlug);
                        final List<CategoryFilter.FilterValue> newFilterValues = new ArrayList<>();
                        for (CategoryFilter.FilterValue filterValue : filterItemToUpdate.selectedValues) {
                            newFilterValues.add()
                        }
                        filtersAndValues.put(selectedValueItem.categorySlug, new FilterAdapterItem())


                    }
                })


    }

    public class FilterValueAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final CategoryFilter.FilterValue filterValue;
        private final boolean isSelected;
        @Nonnull
        private final String categorySlug;

        public FilterValueAdapterItem(@Nonnull CategoryFilter.FilterValue filterValue,
                                      boolean isSelected,
                                      @Nonnull String categorySlug) {
            this.filterValue = filterValue;
            this.isSelected = isSelected;
            this.categorySlug = categorySlug;
        }

        @Nonnull
        public FilterValueAdapterItem withToggledSelection() {
            return new FilterValueAdapterItem(filterValue, !isSelected, categorySlug);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }
    }

    public class FilterAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final CategoryFilter categoryFilter;
        @Nonnull
        private final List<CategoryFilter.FilterValue> selectedValues;

        public FilterAdapterItem(@Nonnull CategoryFilter categoryFilter,
                                         @Nonnull List<CategoryFilter.FilterValue> selectedValues) {
            this.categoryFilter = categoryFilter;
            this.selectedValues = selectedValues;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }
    }


/*    public class Filter {

        @Nonnull
        private final String name;
        @Nonnull
        private final String slug;
        @Nonnull
        private final List<FilterValue> values;
        private boolean isVisible;

        public Filter(@Nonnull String name, @Nonnull String slug,
                      @Nonnull List<FilterValue> values, boolean isVisible) {
            this.name = name;
            this.slug = slug;
            this.values = values;
            this.isVisible = isVisible;
        }

        public class FilterValue {
            @Nonnull
            private final String name;
            @Nonnull
            private final String slug;
            private final boolean isSelected;

            public FilterValue(@Nonnull String name, @Nonnull String slug, boolean isSelected) {
                this.name = name;
                this.slug = slug;
                this.isSelected = isSelected;
            }

            @Nonnull
            public String getName() {
                return name;
            }

            @Nonnull
            public String getSlug() {
                return slug;
            }

            public boolean isSelected() {
                return isSelected;
            }

            public FilterValue withToggledSelection() {
                return new FilterValue(name, slug, !isSelected);
            }
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nonnull
        public List<FilterValue> getValues() {
            return values;
        }

        @Nonnull
        public String getSlug() {
            return slug;
        }

        public Filter withToggledVisivility() {
            return new Filter(name, slug, values, isVisible);
        }
    }*/


}
