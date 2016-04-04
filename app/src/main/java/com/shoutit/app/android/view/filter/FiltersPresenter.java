package com.shoutit.app.android.view.filter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
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
import rx.functions.Func7;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class FiltersPresenter {

    private static final String DEFAULT_CATEGORY_SLUG = "all_categories_slug";

    private final PublishSubject<Object> filterVisibilityChanged = PublishSubject.create();
    private final PublishSubject<AdapterFilterValue> filterValueSelectionChanged = PublishSubject.create();
    private final PublishSubject<String> selectedCategorySubject = PublishSubject.create();
    private final PublishSubject<String> shoutTypeSelectedSubject = PublishSubject.create();
    private final PublishSubject<Object> locationChangeClickSubject = PublishSubject.create();
    private final PublishSubject<Object> doneClickedSubject = PublishSubject.create();
    private final PublishSubject<Object> resetClickedSubject = PublishSubject.create();

    private final BehaviorSubject<UserLocation> locationSelectedSubject = BehaviorSubject.create();
    private final BehaviorSubject<SortType> sortTypeSelectedSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> distanceSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> startPriceSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> endPriceSubject = BehaviorSubject.create();

    private final Observable<List<BaseAdapterItem>> allAdapterItems;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<ImmutableMultimap<String, CategoryFilter.FilterValue>> selectedValuesMapObservable;

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
                .filter(Functions1.isNotNull());

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
                        final HashMap<String, Category> linkedHashMap = new LinkedHashMap<>();
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
                .compose(ObservableExtensions.<ImmutableMultimap<String, CategoryFilter.FilterValue>>behaviorRefCount());

        final Observable<Map<String, FiltersAdapterItems.FilterAdapterItem>> filterItems = selectedCategoryObservable
                .compose(MoreOperators.<Category>refresh(resetClickedSubject))
                .map(new Func1<Category, List<CategoryFilter>>() {
                    @Override
                    public List<CategoryFilter> call(Category category) {
                        return category.getFilters();
                    }
                })
                .map(new Func1<List<CategoryFilter>, Map<String, FiltersAdapterItems.FilterAdapterItem>>() {
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
                .compose(ObservableExtensions.<Map<String, FiltersAdapterItems.FilterAdapterItem>>behaviorRefCount());

        /** Filter Adapter Items **/
        final Observable<List<BaseAdapterItem>> initFilterAdapterItems = filterItems
                .map(mapToFilterWithFilterValuesAdapterItems())
                .compose(ObservableExtensions.<List<BaseAdapterItem>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> changedFilterAdapterItems =
                filterVisibilityChanged
                        .switchMap(new Func1<Object, Observable<Map<String, FiltersAdapterItems.FilterAdapterItem>>>() {
                            @Override
                            public Observable<Map<String, FiltersAdapterItems.FilterAdapterItem>> call(Object o) {
                                return filterItems;
                            }
                        })
                        .map(mapToFilterWithFilterValuesAdapterItems());

        final Observable<List<BaseAdapterItem>> filterAdapterItems = Observable
                .merge(initFilterAdapterItems, changedFilterAdapterItems);

        /** All Adapter Items **/
        allAdapterItems = Observable.combineLatest(
                categoriesSuccessObservable.first(),
                successSortTypes.first(),
                shoutTypeObservable,
                sortTypeObservable.first(),
                selectedCategoryObservable.startWith((Category) null),
                locationObservable,
                filterAdapterItems.startWith(ImmutableList.<BaseAdapterItem>of()),
                new Func7<List<Category>, List<SortType>, String, SortType, Category, UserLocation, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Category> categories, List<SortType> sortTypes,
                                                      String shoutType, SortType sortType,
                                                      Category category, UserLocation userLocation,
                                                      List<BaseAdapterItem> filtersItems) {
                        return ImmutableList.<BaseAdapterItem>builder()
                                .add(new FiltersAdapterItems.HeaderAdapterItem(resetClickedSubject, doneClickedSubject))
                                .add(new FiltersAdapterItems.ShoutTypeAdapterItem(shoutTypeSelectedSubject, shoutType))
                                .add(new FiltersAdapterItems.SortAdapterItem(sortType, sortTypes, sortTypeSelectedSubject))
                                .add(new FiltersAdapterItems.CategoryAdapterItem(category, categories, selectedCategorySubject))
                                .add(new FiltersAdapterItems.PriceAdapterItem(startPriceSubject, endPriceSubject))
                                .add(new FiltersAdapterItems.LocationAdapterItem(userLocation, locationChangeClickSubject))
                                .add(new FiltersAdapterItems.DistanceAdapterItem(distanceSubject))
                                .addAll(filtersItems)
                                .build();
                    }
                });

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
    }

    private Category getDefaultCategory() {
        return new Category(context.getString(R.string.filters_default_category_name),
                DEFAULT_CATEGORY_SLUG, null, null, ImmutableList.<CategoryFilter>of());
    }

    @NonNull
    private Func1<Map<String, FiltersAdapterItems.FilterAdapterItem>, List<BaseAdapterItem>> mapToFilterWithFilterValuesAdapterItems() {
        return new Func1<Map<String, FiltersAdapterItems.FilterAdapterItem>, List<BaseAdapterItem>>() {
            @Override
            public List<BaseAdapterItem> call(Map<String, FiltersAdapterItems.FilterAdapterItem> itemsMap) {
                final ImmutableList.Builder<BaseAdapterItem> allItemsBuilder = ImmutableList.builder();

                final Collection<FiltersAdapterItems.FilterAdapterItem> filtersItems = itemsMap.values();

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
}
