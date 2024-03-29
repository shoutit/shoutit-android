package com.shoutit.app.android.view.filter;

import android.content.Context;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.FilterValue;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.view.search.SearchPresenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Mockito.when;

public class FiltersPresenterTest {

    @Mock
    CategoriesDao categoriesDao;
    @Mock
    SortTypesDao sortTypesDao;
    @Mock
    UserPreferences userPreferences;
    @Mock
    Context context;

    private FiltersPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final List<SortType> sortTypes = Lists.newArrayList(new SortType("type", "name"));
        when(sortTypesDao.sortTypesObservable())
                .thenReturn(Observable.just(ResponseOrError.fromData(sortTypes)));

        when(categoriesDao.categoriesObservable())
                .thenReturn(Observable.just(ResponseOrError.fromData(getCategories())));

        when(userPreferences.getLocation())
                .thenReturn(new UserLocation(2d, 3d, "PL", null, null, null, null));

        presenter = new FiltersPresenter(categoriesDao, sortTypesDao, Schedulers.immediate(), context, userPreferences, SearchPresenter.SearchType.BROWSE, "");
    }

    @Test
    public void testOnSubscribeWithoutCategories_correctItemsReturned() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        final List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FiltersAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FiltersAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FiltersAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FiltersAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FiltersAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FiltersAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FiltersAdapterItems.DistanceAdapterItem.class);
    }

    @Test
    public void testOnSubscribeWithCategories_correctItemsReturned() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);
        presenter.getSelectedCategoryObserver().onNext("slug");

        testSubscriber.assertNoErrors();
        final List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FiltersAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FiltersAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FiltersAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FiltersAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FiltersAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FiltersAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FiltersAdapterItems.DistanceAdapterItem.class);
        assert_().that(lastEvent.get(7)).isInstanceOf(FiltersAdapterItems.FilterAdapterItem.class);
    }

    @Test
    public void testWhenValuesShowClicked_valuesShown() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);
        presenter.getSelectedCategoryObserver().onNext("slug");

        testSubscriber.assertNoErrors();
        List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(7)).isInstanceOf(FiltersAdapterItems.FilterAdapterItem.class);
        ((FiltersAdapterItems.FilterAdapterItem) lastEvent.get(7)).onVisibilityChanged();

        lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FiltersAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FiltersAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FiltersAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FiltersAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FiltersAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FiltersAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FiltersAdapterItems.DistanceAdapterItem.class);
        assert_().that(lastEvent.get(7)).isInstanceOf(FiltersAdapterItems.FilterAdapterItem.class);
        assert_().that(lastEvent.get(8)).isInstanceOf(FiltersAdapterItems.FilterValueAdapterItem.class);
    }

    @Test
    public void testWhenValuesShowedAndHidden_valuesHidden() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);
        presenter.getSelectedCategoryObserver().onNext("slug");

        testSubscriber.assertNoErrors();
        List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(7)).isInstanceOf(FiltersAdapterItems.FilterAdapterItem.class);
        ((FiltersAdapterItems.FilterAdapterItem) lastEvent.get(7)).onVisibilityChanged();
        lastEvent = getLastEvent(testSubscriber);
        ((FiltersAdapterItems.FilterAdapterItem) lastEvent.get(7)).onVisibilityChanged();

        lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FiltersAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FiltersAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FiltersAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FiltersAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FiltersAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FiltersAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FiltersAdapterItems.DistanceAdapterItem.class);
        assert_().that(lastEvent.get(7)).isInstanceOf(FiltersAdapterItems.FilterAdapterItem.class);
    }

    @Test
    public void testWhenResetClicked_NoFiltersShown() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);
        presenter.getSelectedCategoryObserver().onNext("slug");

        testSubscriber.assertNoErrors();
        List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(7)).isInstanceOf(FiltersAdapterItems.FilterAdapterItem.class);
        ((FiltersAdapterItems.FilterAdapterItem) lastEvent.get(7)).onVisibilityChanged();
        ((FiltersAdapterItems.HeaderAdapterItem) lastEvent.get(0)).onResetClicked();

        lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FiltersAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FiltersAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FiltersAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FiltersAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FiltersAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FiltersAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FiltersAdapterItems.DistanceAdapterItem.class);
    }

    private List<Category> getCategories() {
        final List<FilterValue> filterValues = Lists.newArrayList(new FilterValue(null, "name", "slug"));
        final List<CategoryFilter> categoryFilters = Lists.newArrayList(new CategoryFilter("name", "slug", filterValues));
        return Lists.newArrayList(new Category("name", "slug", null, null, categoryFilters));
    }

    private List<BaseAdapterItem> getLastEvent(TestSubscriber<List<BaseAdapterItem>> subscriber) {
        return Iterables.getLast(subscriber.getOnNextEvents());
    }
}
