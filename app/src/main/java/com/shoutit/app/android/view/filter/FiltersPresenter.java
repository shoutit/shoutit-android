package com.shoutit.app.android.view.filter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorSampleWithLastWithObservable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.model.FiltersToSubmit;
import com.shoutit.app.android.view.search.SearchPresenter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func7;
import rx.functions.Func8;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class FiltersPresenter {

    private static final String DEFAULT_CATEGORY_SLUG = "all_categories_slug";

    private final PublishSubject<BothParams<String, Boolean>> filterVisibilityChanged = PublishSubject.create();
    private final PublishSubject<AdapterFilterValue> filterValueSelectionChanged = PublishSubject.create();
    private final PublishSubject<String> selectedCategorySubject = PublishSubject.create();
    private final PublishSubject<String> shoutTypeSelectedSubject = PublishSubject.create();
    private final PublishSubject<Object> locationChangeClickSubject = PublishSubject.create();
    private final PublishSubject<Object> doneClickedSubject = PublishSubject.create();
    private final PublishSubject<Object> resetClickedSubject = PublishSubject.create();
    private final PublishSubject<UserLocation> locationSelectedSubject = PublishSubject.create();

    private final BehaviorSubject<SortType> sortTypeSelectedSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> distanceValueNumberSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> minPriceSubject = BehaviorSubject.create((String) null);
    private final BehaviorSubject<String> maxPriceSubject = BehaviorSubject.create((String) null);

    private final Observable<List<BaseAdapterItem>> allAdapterItems;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<ImmutableMultimap<String, CategoryFilter.FilterValue>> selectedValuesMapObservable;
    private final Observable<FiltersToSubmit> selectedFiltersObservable;

    private static final Integer[] distanceValuesTable = new Integer[] {
            1, 1, 2, 3, 5, 7, 10, 15, 20, 30, 60, 100, 200, 300, 400, 500, 0
    };

    @Nonnull
    private final Context context;
    @Nonnull
    private final SearchPresenter.SearchType searchType;
    @Nullable
    private final String initCategorySlug;

    public FiltersPresenter(@Nonnull CategoriesDao categoriesDao,
                            @Nonnull SortTypesDao sortTypesDao,
                            @Nonnull @UiScheduler Scheduler uiScheduler,
                            @Nonnull @ForActivity Context context,
                            @Nonnull UserPreferences userPreferences,
                            @Nonnull SearchPresenter.SearchType searchType,
                            @Nullable final String initCategorySlug) {
        this.context = context;
        this.searchType = searchType;
        this.initCategorySlug = initCategorySlug;

        final Observable<String> shoutTypeObservable = shoutTypeSelectedSubject
                .startWith(Shout.TYPE_ALL)
                .compose(MoreOperators.<String>refresh(resetClickedSubject))
                .compose(ObservableExtensions.<String>behaviorRefCount());

        final Observable<UserLocation> locationObservable = locationSelectedSubject
                .startWith(Observable.just(userPreferences.getLocation()))
                .compose(MoreOperators.<UserLocation>refresh(resetClickedSubject))
                .filter(Functions1.isNotNull())
                .compose(ObservableExtensions.<UserLocation>behaviorRefCount());

        final Observable<Integer> distanceObservable = distanceValueNumberSubject
                .startWith(0)
                .compose(MoreOperators.<Integer>refresh(resetClickedSubject))
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        return distanceValuesTable[integer];
                    }
                })
                .compose(ObservableExtensions.<Integer>behaviorRefCount());

        /** Sort types request **/
        final Observable<ResponseOrError<List<SortType>>> sortTypesObservable = sortTypesDao.sortTypesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<List<SortType>>>behaviorRefCount());

        final Observable<List<SortType>> successSortTypes = sortTypesObservable
                .compose(ResponseOrError.<List<SortType>>onlySuccess());

        final Observable<SortType> sortTypeObservable = successSortTypes
                .compose(MoreOperators.<List<SortType>>refresh(resetClickedSubject))
                .map(new Func1<List<SortType>, SortType>() {
                    @Override
                    public SortType call(List<SortType> sortTypes) {
                        return sortTypes.get(0);
                    }
                })
                .mergeWith(sortTypeSelectedSubject)
                .compose(ObservableExtensions.<SortType>behaviorRefCount());

        /** Categories **/
        final Observable<ResponseOrError<List<Category>>> categoriesObservable = categoriesDao
                .categoriesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<List<Category>>>behaviorRefCount());

        final Observable<List<Category>> categoriesSuccessObservable = categoriesObservable
                .compose(ResponseOrError.<List<Category>>onlySuccess());

        final Observable<HashMap<String, Category>> successCategoriesObservable = categoriesSuccessObservable
                .map(new Func1<List<Category>, HashMap<String, Category>>() {
                    @Override
                    public HashMap<String, Category> call(List<Category> categories) {
                        final LinkedHashMap<String, Category> linkedHashMap = new LinkedHashMap<>();
                        linkedHashMap.put(DEFAULT_CATEGORY_SLUG, getDefaultCategory());
                        for (Category category : categories) {
                            linkedHashMap.put(category.getSlug(), category);
                        }

                        return linkedHashMap;
                    }
                })
                .compose(ObservableExtensions.<HashMap<String, Category>>behaviorRefCount());

        final Observable<Category> initCategoryObservable = successCategoriesObservable
                .map(new Func1<HashMap<String, Category>, Category>() {
                    @Override
                    public Category call(HashMap<String, Category> categoriesMap) {
                        if (shouldBlockCategories()) {
                            return categoriesMap.get(initCategorySlug);
                        } else {
                            return getDefaultCategory();
                        }
                    }
                });

        final Observable<Category> selectedCategoryObservable = selectedCategorySubject
                .withLatestFrom(successCategoriesObservable,
                        new Func2<String, HashMap<String, Category>, Category>() {
                            @Override
                            public Category call(String categorySlug, HashMap<String, Category> categoriesMap) {
                                return categoriesMap.get(categorySlug);
                            }
                        })
                .startWith(initCategoryObservable.first())
                .compose(MoreOperators.<Category>refresh(resetClickedSubject))
                .compose(ObservableExtensions.<Category>behaviorRefCount());

        /** Filters **/
        selectedValuesMapObservable = filterValueSelectionChanged
                .scan(ImmutableMultimap.<String, CategoryFilter.FilterValue>of(),
                        new Func2<ImmutableMultimap<String, CategoryFilter.FilterValue>, AdapterFilterValue, ImmutableMultimap<String, CategoryFilter.FilterValue>>() {
                            @Override
                            public ImmutableMultimap<String, CategoryFilter.FilterValue> call(ImmutableMultimap<String, CategoryFilter.FilterValue> selectedValues,
                                                                                              AdapterFilterValue changedValue) {
                                final Multimap<String, CategoryFilter.FilterValue> newMap = LinkedHashMultimap.create(selectedValues);
                                final Collection<CategoryFilter.FilterValue> filterValues = newMap.get(changedValue.filter.getSlug());

                                boolean wasValueVisible = false;
                                if (filterValues != null) {
                                    for (CategoryFilter.FilterValue value : filterValues) {
                                        if (value.getSlug().equals(changedValue.filterValue.getSlug())) {
                                            wasValueVisible = true;
                                            break;
                                        }
                                    }
                                    if (wasValueVisible) {
                                        newMap.remove(changedValue.filter.getSlug(), changedValue.filterValue);
                                    } else {
                                        newMap.put(changedValue.filter.getSlug(), changedValue.filterValue);
                                    }
                                }

                                return ImmutableMultimap.copyOf(newMap);
                            }
                        })
                .compose(MoreOperators.<ImmutableMultimap<String, CategoryFilter.FilterValue>>refresh(Observable.merge(resetClickedSubject, selectedCategoryObservable)))
                .startWith(ImmutableMultimap.<String, CategoryFilter.FilterValue>of())
                .compose(ObservableExtensions.<ImmutableMultimap<String, CategoryFilter.FilterValue>>behaviorRefCount());

        final Observable<ImmutableMap<String, Boolean>> filtersVisibilityMap = filterVisibilityChanged
                .scan(ImmutableMap.<String, Boolean>of(),
                        new Func2<ImmutableMap<String, Boolean>, BothParams<String, Boolean>, ImmutableMap<String, Boolean>>() {
                            @Override
                            public ImmutableMap<String, Boolean> call(ImmutableMap<String, Boolean> visibilityMap,
                                                                      BothParams<String, Boolean> changedVisibility) {
                                final Map<String, Boolean> newMap = new LinkedHashMap<>();
                                newMap.putAll(visibilityMap);
                                newMap.put(changedVisibility.param1(), changedVisibility.param2());

                                return ImmutableMap.copyOf(newMap);
                            }
                        })
                .startWith(ImmutableMap.<String, Boolean>of())
                .compose(MoreOperators.<ImmutableMap<String, Boolean>>refresh(Observable.merge(resetClickedSubject, selectedCategoryObservable)))
                .compose(ObservableExtensions.<ImmutableMap<String, Boolean>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> filterAdapterItems = selectedCategoryObservable
                .map(new Func1<Category, List<CategoryFilter>>() {
                    @Override
                    public List<CategoryFilter> call(Category category) {
                        return category.getFilters();
                    }
                })
                .switchMap(new Func1<List<CategoryFilter>, Observable<BothParams<List<CategoryFilter>, Map<String, Boolean>>>>() {
                    @Override
                    public Observable<BothParams<List<CategoryFilter>, Map<String, Boolean>>> call(final List<CategoryFilter> categoryFilters) {
                        return filtersVisibilityMap.map(new Func1<ImmutableMap<String, Boolean>, BothParams<List<CategoryFilter>, Map<String, Boolean>>>() {
                            @Override
                            public BothParams<List<CategoryFilter>, Map<String, Boolean>> call(ImmutableMap<String, Boolean> filtersVisibilityMap) {
                                return new BothParams(categoryFilters, filtersVisibilityMap);
                            }
                        });
                    }
                })
                .map(mapToFilterWithFilterValuesAdapterItems());


        /** All Adapter Items **/
        allAdapterItems = Observable.combineLatest(
                successCategoriesObservable.take(1),
                successSortTypes.take(1),
                shoutTypeObservable,
                sortTypeObservable.take(1),
                selectedCategoryObservable,
                locationObservable,
                filterAdapterItems.startWith(ImmutableList.<BaseAdapterItem>of()),
                new Func7<Map<String, Category>, List<SortType>, String, SortType, Category, UserLocation, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(Map<String, Category> categories, List<SortType> sortTypes,
                                                      String shoutType, SortType sortType,
                                                      Category category, UserLocation userLocation,
                                                      List<BaseAdapterItem> filtersItems) {
                        return ImmutableList.<BaseAdapterItem>builder()
                                .add(new FiltersAdapterItems.HeaderAdapterItem(resetClickedSubject, doneClickedSubject))
                                .add(new FiltersAdapterItems.ShoutTypeAdapterItem(shoutTypeSelectedSubject, shoutType))
                                .add(new FiltersAdapterItems.SortAdapterItem(sortType, sortTypes, sortTypeSelectedSubject, sortTypeObservable, resetClickedSubject))
                                .add(new FiltersAdapterItems.CategoryAdapterItem(category, categories, selectedCategorySubject, selectedCategoryObservable, shouldBlockCategories()))
                                .add(new FiltersAdapterItems.PriceAdapterItem(minPriceSubject, maxPriceSubject, resetClickedSubject))
                                .add(new FiltersAdapterItems.LocationAdapterItem(userLocation, locationChangeClickSubject))
                                .add(new FiltersAdapterItems.DistanceAdapterItem(distanceValueNumberSubject, resetClickedSubject))
                                .addAll(filtersItems)
                                .build();
                    }
                });

        /** Progress and Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(categoriesObservable),
                        ResponseOrError.transform(sortTypesObservable)
                )
        ).filter(Functions1.isNotNull());

        progressObservable = Observable.zip(successSortTypes, successCategoriesObservable, new Func2<List<SortType>, HashMap<String, Category>, Boolean>() {
            @Override
            public Boolean call(List<SortType> sortTypes, HashMap<String, Category> stringCategoryHashMap) {
                return false;
            }
        }).mergeWith(errorObservable.map(Functions1.returnFalse()));

        /** Submit form **/
        selectedFiltersObservable = Observable.combineLatest(
                minPriceSubject,
                maxPriceSubject,
                locationObservable,
                distanceObservable,
                shoutTypeObservable,
                sortTypeObservable,
                selectedValuesMapObservable,
                selectedCategoryObservable,
                new Func8<String, String, UserLocation, Integer, String, SortType, Multimap<String, CategoryFilter.FilterValue>, Category, FiltersToSubmit>() {
                    @Override
                    public FiltersToSubmit call(String minPrice,
                                                String maxPrice,
                                                UserLocation userLocation,
                                                Integer distance,
                                                String shoutType,
                                                SortType sortType,
                                                Multimap<String, CategoryFilter.FilterValue> selectedValues,
                                                Category category) {
                        final String categorySlug = DEFAULT_CATEGORY_SLUG.equals(category.getSlug()) ? null : category.getSlug();
                        return new FiltersToSubmit(minPrice, maxPrice, userLocation, distance, shoutType, sortType, selectedValues, categorySlug);
                    }
                })
                .lift(OperatorSampleWithLastWithObservable.<FiltersToSubmit>create(doneClickedSubject));
    }

    private boolean shouldBlockCategories() {
        return SearchPresenter.SearchType.CATEGORY.equals(searchType) && initCategorySlug != null;
    }

    @NonNull
    private Func1<BothParams<List<CategoryFilter>, Map<String, Boolean>>, List<BaseAdapterItem>> mapToFilterWithFilterValuesAdapterItems() {
        return new Func1<BothParams<List<CategoryFilter>, Map<String, Boolean>>, List<BaseAdapterItem>>() {
            @Override
            public List<BaseAdapterItem> call(BothParams<List<CategoryFilter>, Map<String, Boolean>> filtersWithVisibility) {
                final ImmutableList.Builder<BaseAdapterItem> allItemsBuilder = ImmutableList.builder();

                final List<CategoryFilter> categoryFilters = filtersWithVisibility.param1();
                final Map<String, Boolean> filtersVisibilityMap = filtersWithVisibility.param2();

                for (CategoryFilter categoryFilter : categoryFilters) {

                    final Boolean value = filtersVisibilityMap.get(categoryFilter.getSlug());
                    boolean hasVisibleValues = value != null && value;
                    allItemsBuilder.add(new FiltersAdapterItems.FilterAdapterItem(
                            categoryFilter, filterVisibilityChanged,
                            selectedValuesMapObservable, hasVisibleValues));

                    if (hasVisibleValues) {
                        for (CategoryFilter.FilterValue filterValue : categoryFilter.getValues()) {
                            allItemsBuilder.add(new FiltersAdapterItems.FilterValueAdapterItem(
                                    categoryFilter, filterValue, filterValueSelectionChanged, selectedValuesMapObservable));
                        }
                    }
                }

                return allItemsBuilder.build();
            }
        };
    }

    private Category getDefaultCategory() {
        return new Category(context.getString(R.string.filters_default_category_name),
                DEFAULT_CATEGORY_SLUG, null, null, ImmutableList.<CategoryFilter>of());
    }

    public Observable<FiltersToSubmit> getSelectedFiltersObservable() {
        return selectedFiltersObservable;
    }

    public Observable<Object> getLocationChangeClickObservable() {
        return locationChangeClickSubject;
    }

    public Observable<List<BaseAdapterItem>> getAllAdapterItems() {
        return allAdapterItems;
    }

    public Observer<String> getSelectedCategoryObserver() {
        return selectedCategorySubject;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }



    public void onLocationChanged(UserLocation userLocation) {
        locationSelectedSubject.onNext(userLocation);
    }

    public static class AdapterFilterValue {
        private final CategoryFilter filter;
        private final CategoryFilter.FilterValue filterValue;

        public AdapterFilterValue(CategoryFilter filter, CategoryFilter.FilterValue filterValue) {
            this.filter = filter;
            this.filterValue = filterValue;
        }
    }
}
