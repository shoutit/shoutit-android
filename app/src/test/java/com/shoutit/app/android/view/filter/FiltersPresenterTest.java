package com.shoutit.app.android.view.filter;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.SortTypesDao;

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

        presenter = new FiltersPresenter(categoriesDao, sortTypesDao, Schedulers.immediate(), userPreferences);
    }

    @Test
    public void testOnSubscribeWithoutCategories_correctItemsReturned() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        final List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FilterAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FilterAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FilterAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FilterAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FilterAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FilterAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FilterAdapterItems.DistanceAdapterItem.class);
    }

    @Test
    public void testOnSubscribeWithCategories_correctItemsReturned() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);
        presenter.getSelectedCategoryObserver().onNext("slug");

        testSubscriber.assertNoErrors();
        final List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FilterAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FilterAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FilterAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FilterAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FilterAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FilterAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FilterAdapterItems.DistanceAdapterItem.class);
        assert_().that(lastEvent.get(7)).isInstanceOf(FilterAdapterItems.FilterAdapterItem.class);
    }

    @Test
    public void testWhenValuesShowClicked_valuesShown() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);
        presenter.getSelectedCategoryObserver().onNext("slug");

        testSubscriber.assertNoErrors();
        List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(7)).isInstanceOf(FilterAdapterItems.FilterAdapterItem.class);
        ((FilterAdapterItems.FilterAdapterItem) lastEvent.get(7)).onVisibilityChanged();

        lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FilterAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FilterAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FilterAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FilterAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FilterAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FilterAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FilterAdapterItems.DistanceAdapterItem.class);
        assert_().that(lastEvent.get(7)).isInstanceOf(FilterAdapterItems.FilterAdapterItem.class);
        assert_().that(lastEvent.get(8)).isInstanceOf(FilterAdapterItems.FilterValueAdapterItem.class);
    }

    @Test
    public void testWhenValuesShowedAndHidden_valuesHidden() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);
        presenter.getSelectedCategoryObserver().onNext("slug");

        testSubscriber.assertNoErrors();
        List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(7)).isInstanceOf(FilterAdapterItems.FilterAdapterItem.class);
        ((FilterAdapterItems.FilterAdapterItem) lastEvent.get(7)).onVisibilityChanged();
        lastEvent = getLastEvent(testSubscriber);
        ((FilterAdapterItems.FilterAdapterItem) lastEvent.get(7)).onVisibilityChanged();

        lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FilterAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FilterAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FilterAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FilterAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FilterAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FilterAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FilterAdapterItems.DistanceAdapterItem.class);
        assert_().that(lastEvent.get(7)).isInstanceOf(FilterAdapterItems.FilterAdapterItem.class);
    }

    @Test
    public void testWhenResetClicked_NoFiltersShown() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> testSubscriber = new TestSubscriber<>();

        presenter.getAllAdapterItems().subscribe(testSubscriber);
        presenter.getSelectedCategoryObserver().onNext("slug");

        testSubscriber.assertNoErrors();
        List<BaseAdapterItem> lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(7)).isInstanceOf(FilterAdapterItems.FilterAdapterItem.class);
        ((FilterAdapterItems.FilterAdapterItem) lastEvent.get(7)).onVisibilityChanged();
        ((FilterAdapterItems.HeaderAdapterItem) lastEvent.get(0)).onResetClicked();

        lastEvent = getLastEvent(testSubscriber);
        assert_().that(lastEvent.get(0)).isInstanceOf(FilterAdapterItems.HeaderAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(FilterAdapterItems.ShoutTypeAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(FilterAdapterItems.SortAdapterItem.class);
        assert_().that(lastEvent.get(3)).isInstanceOf(FilterAdapterItems.CategoryAdapterItem.class);
        assert_().that(lastEvent.get(4)).isInstanceOf(FilterAdapterItems.PriceAdapterItem.class);
        assert_().that(lastEvent.get(5)).isInstanceOf(FilterAdapterItems.LocationAdapterItem.class);
        assert_().that(lastEvent.get(6)).isInstanceOf(FilterAdapterItems.DistanceAdapterItem.class);
        assert_().that(lastEvent.get(7)).isInstanceOf(FilterAdapterItems.FilterAdapterItem.class);
    }

    private List<Category> getCategories() {
        final List<CategoryFilter.FilterValue> filterValues = Lists.newArrayList(new CategoryFilter.FilterValue("name", "slug"));
        final List<CategoryFilter> categoryFilters = Lists.newArrayList(new CategoryFilter("name", "slug", filterValues));
        return Lists.newArrayList(new Category("name", "slug", null, null, categoryFilters));
    }

    private List<BaseAdapterItem> getLastEvent(TestSubscriber<List<BaseAdapterItem>> subscriber) {
        return Iterables.getLast(subscriber.getOnNextEvents());
    }
}
