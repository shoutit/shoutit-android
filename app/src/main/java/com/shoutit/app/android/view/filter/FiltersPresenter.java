package com.shoutit.app.android.view.filter;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.utils.MoreFunctions1;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func5;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class FiltersPresenter {

    private final PublishSubject<Object> filterVisibilityChanged = PublishSubject.create();
    private final PublishSubject<Object> filterValueSelectionChanged = PublishSubject.create();
    private final PublishSubject<String> selectedCategorySubject = PublishSubject.create();
    private final PublishSubject<String> shoutTypeSelectedSubject = PublishSubject.create();
    private final PublishSubject<UserLocation> locationSelectedSubject = PublishSubject.create();
    private final PublishSubject<Object> locationChangeClickSubject = PublishSubject.create();
    private final PublishSubject<Object> doneClickedSubject = PublishSubject.create();
    private final PublishSubject<Object> resetClickedSubject = PublishSubject.create();
    private final PublishSubject<SortType> sortTypeSelectedSubject = PublishSubject.create();
    private final PublishSubject<Object> sortTypeChangeClickedObserver = PublishSubject.create();

    private final BehaviorSubject<String> startPriceSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> endPriceSubject = BehaviorSubject.create();


    public FiltersPresenter(@Nonnull CategoriesDao categoriesDao,
                            @Nonnull SortTypesDao sortTypesDao,
                            @Nonnull @UiScheduler Scheduler uiScheduler,
                            @Nonnull UserPreferences userPreferences) {

        final Observable<String> shoutTypeObservable = shoutTypeSelectedSubject
                .startWith(Shout.TYPE_ALL)
                .compose(ObservableExtensions.<String>behaviorRefCount());

        final Observable<UserLocation> locationObservable = locationSelectedSubject
                .startWith(Observable.just(userPreferences.getLocation()))
                .filter(Functions1.isNotNull());

        /** Sort types request **/
        final Observable<ResponseOrError<List<SortType>>> sortTypes = sortTypesDao.sortTypesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<List<SortType>>>behaviorRefCount());

        final Observable<List<SortType>> sortTypesObservable = sortTypes.compose(ResponseOrError.<List<SortType>>onlySuccess());

        Observable<SortType> sortTypeObservable = sortTypesObservable
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

        final Observable<Category> selectedCategoryObservable = selectedCategorySubject
                .withLatestFrom(successCategoriesObservable,
                        new Func2<String, HashMap<String, Category>, Category>() {
                            @Override
                            public Category call(String categorySlug, HashMap<String, Category> categoriesMap) {
                                return categoriesMap.get(categorySlug);
                            }
                        })
                .compose(ObservableExtensions.<Category>behaviorRefCount());

        /** Selected category filters map **/
        final Observable<Multimap<String, BaseAdapterItem>> filterItems = selectedCategoryObservable
                .compose(MoreOperators.<Category>refresh(resetClickedSubject))
                .map(new Func1<Category, List<CategoryFilter>>() {
                    @Override
                    public List<CategoryFilter> call(Category category) {
                        return category.getFilters();
                    }
                })
                .filter(MoreFunctions1.<CategoryFilter>listNotEmpty())
                .map(new Func1<List<CategoryFilter>, Multimap<String, BaseAdapterItem>>() {
                    @Override
                    public Multimap<String, BaseAdapterItem> call(List<CategoryFilter> categoryFilters) {
                        // Multimap filterSlug -> [CategoryFilter, CategoryValueFilter, CategoryValueFilter]
                        final ImmutableListMultimap.Builder<String, BaseAdapterItem> builder = ImmutableListMultimap.builder();

                        for (CategoryFilter filter : categoryFilters) {

                            builder.put(
                                    filter.getSlug(),
                                    new FilterAdapterItems.FilterAdapterItem(filter, ImmutableList.<CategoryFilter.FilterValue>of(), filterVisibilityChanged)
                            );

                            for (CategoryFilter.FilterValue value : filter.getValues()) {
                                builder.put(
                                        filter.getSlug(),
                                        new FilterAdapterItems.FilterValueAdapterItem(value, filterValueSelectionChanged)
                                );
                            }
                        }

                        return builder.build();
                    }
                })
                .compose(ObservableExtensions.<Multimap<String, BaseAdapterItem>>behaviorRefCount());

        /** Filter Adapter Items **/
        final Observable<List<BaseAdapterItem>> filterAdapterItems = Observable.merge(filterVisibilityChanged, filterValueSelectionChanged)
                .withLatestFrom(filterItems, new Func2<Object, Multimap<String, BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(Object o, Multimap<String, BaseAdapterItem> itemsMultiMap) {
                        final ImmutableList.Builder<BaseAdapterItem> allItemsBuilder = ImmutableList.builder();

                        for (final String key : itemsMultiMap.keySet()) {

                            final Collection<BaseAdapterItem> filterItems = itemsMultiMap.get(key);
                            final ImmutableList.Builder<CategoryFilter.FilterValue> filterValuesBuilder = ImmutableList.builder();
                            FilterAdapterItems.FilterAdapterItem filterAdapterItem = null;
                            boolean isFilterVisible = true;

                            for (final BaseAdapterItem item : filterItems) {
                                if (item instanceof FilterAdapterItems.FilterAdapterItem) {
                                    filterAdapterItem = (FilterAdapterItems.FilterAdapterItem) item;
                                    isFilterVisible = ((FilterAdapterItems.FilterAdapterItem) item).isVisible();
                                    allItemsBuilder.add(item);
                                } else if (item instanceof FilterAdapterItems.FilterValueAdapterItem) {
                                    filterValuesBuilder.add(((FilterAdapterItems.FilterValueAdapterItem) item).getFilterValue());
                                    if (isFilterVisible) {
                                        allItemsBuilder.add(item);
                                    }
                                }
                            }

                            assert filterAdapterItem != null;
                            filterAdapterItem.setSelectedValues(filterValuesBuilder.build());
                        }

                        return allItemsBuilder.build();
                    }
                });

        /** All Adapter Items **/
        Observable.combineLatest(shoutTypeObservable, sortTypeObservable,
                selectedCategoryObservable, locationObservable, filterAdapterItems,
                new Func5<String, SortType, Category, UserLocation, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(String shoutType, SortType sortType,
                                                      Category category, UserLocation userLocation,
                                                      List<BaseAdapterItem> filtersItems) {
                        return ImmutableList.<BaseAdapterItem>builder()
                                .add(new FilterAdapterItems.HeaderAdapterItem(resetClickedSubject, doneClickedSubject))
                                .add(new FilterAdapterItems.ShoutTypeAdapterItem(shoutTypeSelectedSubject))
                                .add(new FilterAdapterItems.SortAdapterItem(sortType, sortTypeChangeClickedObserver))
                                .add(new FilterAdapterItems.CategoryAdapterItem(category))
                                .add(new FilterAdapterItems.PriceAdapterItem())
                                .add(new FilterAdapterItems.LocationAdapterItem(userLocation, locationChangeClickSubject))
                                .addAll(filtersItems)
                                .build();
                    }
                });
    }


}
