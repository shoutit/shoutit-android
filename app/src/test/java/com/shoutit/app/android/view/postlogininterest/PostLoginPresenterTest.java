package com.shoutit.app.android.view.postlogininterest;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.Tag;
import com.shoutit.app.android.api.model.TagsRequest;
import com.shoutit.app.android.dao.CategoriesDao;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.observers.TestObserver;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostLoginPresenterTest extends TestCase {

    private PostLoginPresenter mPostLoginPresenter;

    private CategoriesDao mCategoriesDao;

    @Mock
    ApiService mApiService;

    private BehaviorSubject<List<Category>> mSubject;
    private BehaviorSubject<Object> mPostSubject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mSubject = BehaviorSubject.<List<Category>>create(ImmutableList.of(new Category("name", "slug", "icon", new Tag("id", "name", "apiUrl", "image"), filters)));
        when(mApiService.categories()).thenReturn(mSubject);
        mPostSubject = BehaviorSubject.create(new Object());
        when(mApiService.batchListen(any(TagsRequest.class))).thenReturn(mPostSubject);

        mCategoriesDao = new CategoriesDao(mApiService, Schedulers.immediate());
        mPostLoginPresenter = new PostLoginPresenter(mCategoriesDao, mApiService, Schedulers.immediate(), Schedulers.immediate(), new SelectionHelper<String>());
    }

    @Test
    public void testSuccess() {
        TestObserver<List<BaseAdapterItem>> testObserver = new TestObserver<>();

        mPostLoginPresenter.getCategoriesList().subscribe(testObserver);

        assert_().that(testObserver.getOnErrorEvents()).isEmpty();
        assert_().that(testObserver.getOnNextEvents()).hasSize(1);
        assert_().that(Iterables.getLast(testObserver.getOnNextEvents())).isNotEmpty();
    }

    @Test
    public void testFail() {
        mSubject.onError(new RuntimeException());
        TestObserver<Throwable> testObserver = new TestObserver<>();

        mPostLoginPresenter.getErrorObservable().subscribe(testObserver);

        assert_().that(testObserver.getOnNextEvents()).isNotEmpty();
    }

    @Test
    public void testSelection() {
        TestObserver<List<BaseAdapterItem>> testObserver = new TestObserver<>();
        TestObserver<Boolean> testSelectionObserver = new TestObserver<>();


        mPostLoginPresenter.getCategoriesList().subscribe(testObserver);
        final PostLoginPresenter.CategoryItem last = (PostLoginPresenter.CategoryItem) Iterables.getLast(Iterables.getLast(testObserver.getOnNextEvents()));


        last.selection().subscribe(testSelectionObserver);
        assert_().that(Iterables.getLast(testSelectionObserver.getOnNextEvents())).isFalse();

        last.selectionObserver().onNext(true);
        assert_().that(Iterables.getLast(testSelectionObserver.getOnNextEvents())).isTrue();
    }

    @Test
    public void testWhenClickedNext_sendCategories() {
        final TestObserver<List<BaseAdapterItem>> testObserver = new TestObserver<>();
        final TestObserver<Boolean> testSelectionObserver = new TestObserver<>();

        mPostLoginPresenter.getCategoriesList().subscribe(testObserver);
        final PostLoginPresenter.CategoryItem last = (PostLoginPresenter.CategoryItem) Iterables.getLast(Iterables.getLast(testObserver.getOnNextEvents()));

        last.selection().subscribe(testSelectionObserver);
        last.selectionObserver().onNext(true);

        mPostLoginPresenter.getSuccessCategoriesObservable().subscribe();
        mPostLoginPresenter.nextClickedObserver().onNext(new Object());

        verify(mApiService).batchListen(any(TagsRequest.class));
    }


    @Test
    public void testWhenClickedNextAndErrored_sendingAgainWorks() {
        mPostSubject.onError(new RuntimeException());
        final TestObserver<List<BaseAdapterItem>> testObserver = new TestObserver<>();
        final TestObserver<Boolean> testSelectionObserver = new TestObserver<>();
        final TestObserver<Throwable> throwableTestObserver = new TestObserver<>();

        mPostLoginPresenter.getCategoriesList().subscribe(testObserver);
        final PostLoginPresenter.CategoryItem last = (PostLoginPresenter.CategoryItem) Iterables.getLast(Iterables.getLast(testObserver.getOnNextEvents()));

        last.selection().subscribe(testSelectionObserver);
        last.selectionObserver().onNext(true);

        mPostLoginPresenter.getSuccessCategoriesObservable().subscribe();
        mPostLoginPresenter.getPostCategoriesError().subscribe(throwableTestObserver);
        mPostLoginPresenter.nextClickedObserver().onNext(new Object());

        assert_().that(throwableTestObserver.getOnNextEvents()).isNotEmpty();
        verify(mApiService).batchListen(any(TagsRequest.class));

        mPostLoginPresenter.nextClickedObserver().onNext(new Object());

        verify(mApiService, times(2)).batchListen(any(TagsRequest.class));
    }
}