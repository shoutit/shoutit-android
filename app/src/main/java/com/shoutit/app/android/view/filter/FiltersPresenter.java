package com.shoutit.app.android.view.filter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.util.LogTransformer;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func6;
import rx.functions.Func7;
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

    private final BehaviorSubject<UserLocation> locationSelectedSubject = BehaviorSubject.create();
    private final BehaviorSubject<SortType> sortTypeSelectedSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> distanceValueNumberSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> minPriceSubject = BehaviorSubject.create((String) null);
    private final BehaviorSubject<String> maxPriceSubject = BehaviorSubject.create((String) null);
    private final BehaviorSubject<Map<String, Boolean>> visibleFilters = BehaviorSubject.create();

    private final Observable<List<BaseAdapterItem>> allAdapterItems;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<ImmutableMultimap<String, CategoryFilter.FilterValue>> selectedValuesMapObservable;
    private final Observable<FiltersToSubmit> selectedFiltersObservable;

    private static final Map<Integer, Integer> valueToDistanceMap = ImmutableMap.<Integer, Integer>builder()
            .put(0, 1).put(1, 2).put(2, 3).put(3, 5).put(4, 7).put(5, 10)
            .put(6, 15).put(7, 20).put(8, 30).put(9, 60).put(10, 100).put(11, 200).put(12, 300)
            .put(13, 400).put(14, 500).put(15, 0).build();

    @Nonnull
    private final Context context;

    @Inject
    public FiltersPresenter(@Nonnull CategoriesDao categoriesDao,
                            @Nonnull SortTypesDao sortTypesDao,
                            @Nonnull @UiScheduler Scheduler uiScheduler,
                            @Nonnull @ForActivity Context context,
                            @Nonnull UserPreferences userPreferences) {
        this.context = context;

        final Observable<String> shoutTypeObservable = shoutTypeSelectedSubject
                .startWith(Shout.TYPE_ALL)
                .compose(ObservableExtensions.<String>behaviorRefCount());

        final Observable<UserLocation> locationObservable = locationSelectedSubject
                .startWith(Observable.just(userPreferences.getLocation()))
                .filter(Functions1.isNotNull())
                .compose(ObservableExtensions.<UserLocation>behaviorRefCount());

        final Observable<Integer> distanceObservable = distanceValueNumberSubject
                .startWith(0)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        return valueToDistanceMap.get(integer);
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
                .map(new Func1<List<SortType>, SortType>() {
                    @Override
                    public SortType call(List<SortType> sortTypes) {
                        return sortTypes.get(0);
                    }
                })
                .mergeWith(sortTypeSelectedSubject)
                .compose(ObservableExtensions.<SortType>behaviorRefCount());

        /** Categories request **/
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

        final Observable<Category> selectedCategoryObservable = selectedCategorySubject
                .withLatestFrom(successCategoriesObservable,
                        new Func2<String, HashMap<String, Category>, Category>() {
                            @Override
                            public Category call(String categorySlug, HashMap<String, Category> categoriesMap) {
                                return categoriesMap.get(categorySlug);
                            }
                        })
                .startWith(getDefaultCategory())
                .compose(ObservableExtensions.<Category>behaviorRefCount());

        selectedValuesMapObservable = filterValueSelectionChanged
                .scan(ImmutableMultimap.<String, CategoryFilter.FilterValue>of(),
                        new Func2<ImmutableMultimap<String, CategoryFilter.FilterValue>, AdapterFilterValue, ImmutableMultimap<String, CategoryFilter.FilterValue>>() {
                            @Override
                            public ImmutableMultimap<String, CategoryFilter.FilterValue> call(ImmutableMultimap<String, CategoryFilter.FilterValue> selectedValues,
                                                                                              AdapterFilterValue changedValue) {
                                final Multimap<String, CategoryFilter.FilterValue> newMap = LinkedHashMultimap.create(selectedValues);
                                if (changedValue.isSelected) {
                                    newMap.put(changedValue.filter.getSlug(), changedValue.filterValue);
                                } else {
                                    newMap.remove(changedValue.filter.getSlug(), changedValue.filterValue);
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
                .compose(ObservableExtensions.<ImmutableMap<String,Boolean>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> filterItems = selectedCategoryObservable
                .compose(MoreOperators.<Category>refresh(resetClickedSubject))
                .map(new Func1<Category, List<CategoryFilter>>() {
                    @Override
                    public List<CategoryFilter> call(Category category) {
                        return category.getFilters();
                    }
                })
                .switchMap(new Func1<List<CategoryFilter>, Observable<BothParams<List<CategoryFilter>, Map<String, Boolean>>>>() {
                    @Override
                    public Observable<BothParams<List<CategoryFilter>, Map<String, Boolean>>> call(final List<CategoryFilter> categoryFilters) {
                        return filtersVisibilityMap.map(new Func1<ImmutableMap<String, Boolean>, BothParams<List<CategoryFilter>, Map<String, Boolean>>() {
                            @Override
                            public BothParams<List<CategoryFilter>, Map<String, Boolean>> call(ImmutableMap<String, Boolean> filtersVisibilityMap) {
                                return new BothParams(categoryFilters, filtersVisibilityMap);
                            }
                        });
                    }
                })
                .map(mapToFilterWithFilterValuesAdapterItems());


/*                .map(new Func1<List<CategoryFilter>, Map<String, FiltersAdapterItems.FilterAdapterItem>>() {
                    @Override
                    public Map<String, FiltersAdapterItems.FilterAdapterItem> call(List<CategoryFilter> categoryFilters) {
                        final ImmutableMap.Builder<String, FiltersAdapterItems.FilterAdapterItem> builder = ImmutableMap.builder();

                        for (CategoryFilter filter : categoryFilters) {
                            builder.put(
                                    filter.getSlug(),
                                    new FiltersAdapterItems.FilterAdapterItem(filter, ImmutableList.<CategoryFilter.FilterValue>of(),
                                            filterVisibilityChanged, selectedValuesMapObservable)
                            );
                        }

                        return builder.build();
                    }
                })
                .compose(MoreOperators.<Map<String, FiltersAdapterItems.FilterAdapterItem>>cacheWithTimeout(uiScheduler));*/

                        /** Filter Adapter Items **/




   /*     final Observable<List<BaseAdapterItem>> filterAdapterItems = filterItems
                .switchMap(new Func1<Map<String, FiltersAdapterItems.FilterAdapterItem>, Observable<? extends List<BaseAdapterItem>>>() {
                    @Override
                    public Observable<BothParams<ImmutableMap<String, FiltersAdapterItems.FilterAdapterItem>>> call(Map<String, FiltersAdapterItems.FilterAdapterItem> itemsMap) {
                        return filtersVisibilityMap
                                .map(new Func1<ImmutableMap<String, Boolean>, List<BaseAdapterItem>>() {
                                    @Override
                                    public List<BaseAdapterItem> call(ImmutableMap<String, Boolean> stringBooleanImmutableMap) {
                                        return null;
                                    }
                                })
                    }
                })
                .map(mapToFilterWithFilterValuesAdapterItems());*/


                        /** All Adapter Items **/
                        allAdapterItems = Observable.combineLatest(
                                successCategoriesObservable.take(1).compose(LogTransformer.<Map<String, Category>>transformer("lol", "categoriesSuccessObservable")),
                                successSortTypes.take(1).compose(LogTransformer.<List<SortType>>transformer("lol", "successSortTypes")),
                                shoutTypeObservable.compose(LogTransformer.<String>transformer("lol", "shoutType")),
                                sortTypeObservable.take(1).compose(LogTransformer.<SortType>transformer("lol", "sortTypeObservable")),
                                selectedCategoryObservable.startWith((Category) null).compose(LogTransformer.<Category>transformer("lol", "selectedCategory")),
                                locationObservable.compose(LogTransformer.<UserLocation>transformer("lol", "location")),
                                filterAdapterItems.startWith(ImmutableList.<BaseAdapterItem>of()).compose(LogTransformer.<List<BaseAdapterItem>>transformer("lol", "filters")),
                                new Func7<Map<String, Category>, List<SortType>, String, SortType, Category, UserLocation, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                                    @Override
                                    public List<BaseAdapterItem> call(Map<String, Category> categories, List<SortType> sortTypes,
                                                                      String shoutType, SortType sortType,
                                                                      Category category, UserLocation userLocation,
                                                                      List<BaseAdapterItem> filtersItems) {
                                        return ImmutableList.<BaseAdapterItem>builder()
                                                .add(new FiltersAdapterItems.HeaderAdapterItem(resetClickedSubject, doneClickedSubject))
                                                .add(new FiltersAdapterItems.ShoutTypeAdapterItem(shoutTypeSelectedSubject, shoutType))
                                                .add(new FiltersAdapterItems.SortAdapterItem(sortType, sortTypes, sortTypeSelectedSubject))
                                                .add(new FiltersAdapterItems.CategoryAdapterItem(category, categories, selectedCategorySubject, selectedCategoryObservable))
                                                .add(new FiltersAdapterItems.PriceAdapterItem(minPriceSubject, maxPriceSubject))
                                                .add(new FiltersAdapterItems.LocationAdapterItem(userLocation, locationChangeClickSubject))
                                                .add(new FiltersAdapterItems.DistanceAdapterItem(distanceValueNumberSubject))
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
        selectedFiltersObservable = doneClickedSubject
                .switchMap(new Func1<Object, Observable<FiltersToSubmit>>() {
                    @Override
                    public Observable<FiltersToSubmit> call(Object o) {
                        return Observable.combineLatest(
                                minPriceSubject,
                                maxPriceSubject,
                                locationObservable,
                                distanceObservable,
                                shoutTypeObservable,
                                selectedValuesMapObservable,
                                new Func6<String, String, UserLocation, Integer, String, Multimap<String,CategoryFilter.FilterValue>, FiltersToSubmit>() {
                                    @Override
                                    public FiltersToSubmit call(String minPrice,
                                                                String maxPrice,
                                                                UserLocation userLocation,
                                                                Integer distance,
                                                                String shoutType,
                                                                Multimap<String, CategoryFilter.FilterValue> selectedValues) {
                                        return new FiltersToSubmit(minPrice, maxPrice, userLocation, distance, shoutType, selectedValues);
                                    }
                                });
                    }
                });
    }

    @NonNull
    private Func1<BothParams<List<CategoryFilter>, Map<String, Boolean>>, List<BaseAdapterItem>> mapToFilterWithFilterValuesAdapterItems() {
        return new Func1<BothParams<List<CategoryFilter>, Map<String, Boolean>>, List<BaseAdapterItem>>() {
                 @Override
                 public List<BaseAdapterItem> call(BothParams<List<CategoryFilter>, Map<String, Boolean>> filtersWithVisibility) {
                     final ImmutableList.Builder<BaseAdapterItem> allItemsBuilder = ImmutableList.builder();

                     final List<CategoryFilter> categoryFilters = filtersWithVisibility.param1();
                     final Map<String, Boolean> visibilityMap = filtersWithVisibility.param2();
                     for (CategoryFilter categoryFilter : categoryFilters) {

                     }
                     for (FiltersAdapterItems.FilterAdapterItem filterItem : filtersItems) {
                         allItemsBuilder.add(filterItem);

                         if (filterItem.isVisible()) {
                             for (CategoryFilter.FilterValue filterValue : filterItem.getFilterValues()) {
                                 allItemsBuilder.add(new FiltersAdapterItems.FilterValueAdapterItem(
                                         filterItem.getCategoryFilter(), filterValue, filterValueSelectionChanged, selectedValuesMapObservable));
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


    @NonNull
    private Func1<BothParams<List<CategoryFilter>, Map<String, Boolean>>> mapTozFilterWithFilterValuesAdapterItems() {
        return new Func1<BothParams<List<CategoryFilter>, Map<String, Boolean>>>() {
            @Override
            public List<BaseAdapterItem> call(BothParams<List<CategoryFilter>, Map<String, FiltersAdapterItems.FilterAdapterItem>> itemsWithVisibility) {



            }
        };
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
        private final boolean isSelected;

        public AdapterFilterValue(CategoryFilter filter, CategoryFilter.FilterValue filterValue, boolean isSelected) {
            this.filter = filter;
            this.filterValue = filterValue;
            this.isSelected = isSelected;
        }
    }

    public static class FiltersWithVisibilities {
        private final Map<String, FiltersAdapterItems.FilterAdapterItem> filterItems;
    }
}
