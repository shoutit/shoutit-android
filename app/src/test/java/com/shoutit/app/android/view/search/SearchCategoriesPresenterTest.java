package com.shoutit.app.android.view.search;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.dao.CategoriesDao;

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

public class SearchCategoriesPresenterTest {

    @Mock
    CategoriesDao categoriesDao;

    private SearchCategoriesPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final List<Category> categories = Lists.newArrayList(new Category(null, null, null, null, null));
        when(categoriesDao.getListObservableResponseOrError())
                .thenReturn(Observable.just(ResponseOrError.fromData(categories)));

        presenter = new SearchCategoriesPresenter(categoriesDao, Schedulers.immediate());
    }

    @Test
    public void testOnStart_correctDataReturned() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();

        presenter.getCategoriesObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        final List<BaseAdapterItem> lastData = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(lastData.get(0)).isInstanceOf(NoDataAdapterItem.class);
        assert_().that(lastData.get(1)).isInstanceOf(SearchCategoriesPresenter.CategoryAdapterItem.class);
    }

    @Test
    public void testProgressSequence() throws Exception {
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();

        presenter.getProgressObservable().subscribe(subscriber);

        subscriber.assertValues(true, false);
    }

    @Test
    public void testErrorObservable() throws Exception {
        when(categoriesDao.getListObservableResponseOrError())
                .thenReturn(Observable.just(ResponseOrError.<List<Category>>fromError(new Throwable())));
        presenter = new SearchCategoriesPresenter(categoriesDao, Schedulers.immediate());

        TestSubscriber<Throwable> errorSubscriber = new TestSubscriber<>();
        TestSubscriber<Boolean> progressSubscriber = new TestSubscriber<>();

        presenter.getErrorsObservable().subscribe(errorSubscriber);
        presenter.getProgressObservable().subscribe(progressSubscriber);

        progressSubscriber.assertValues(true, false);
        errorSubscriber.assertValueCount(1);
    }

}
