package com.shoutit.app.android.view.home;

import android.content.Context;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.Discover;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.Tag;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.model.LocationPointer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import javax.annotation.Nonnull;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class HomePresenterTest {

    @Mock
    ShoutsDao shoutsDao;
    @Mock
    DiscoversDao discoversDao;
    @Mock
    DiscoversDao.DiscoverItemDao discoverItemDao;
    @Mock
    User user;
    @Mock
    Category category;
    @Mock
    Tag tag;
    @Mock
    UserPreferences userPreferences;
    @Mock
    Context context;

    private HomePresenter presenter;
    private final TestScheduler scheduler = new TestScheduler();
    private final TestSubject<ResponseOrError<ShoutsResponse>> shoutsSubject = TestSubject.create(scheduler);
    private final TestSubject<Object> loadMoreShoutsSubject = TestSubject.create(scheduler);
    private final TestSubject<ResponseOrError<DiscoverResponse>> discoversSubject = TestSubject.create(scheduler);
    private final TestSubject<ResponseOrError<DiscoverItemDetailsResponse>> discoversDetailsSubject = TestSubject.create(scheduler);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(shoutsDao.getHomeShoutsObservable(any(LocationPointer.class))).thenReturn(shoutsSubject);
        when(shoutsDao.getLoadMoreHomeShoutsObserver()).thenReturn(loadMoreShoutsSubject);

        when(discoversDao.getDiscoverItemDao(anyString())).thenReturn(discoverItemDao);
        when(discoversDao.getDiscoverItemDao(anyString()).getDiscoverItemObservable()).thenReturn(discoversDetailsSubject);
        when(discoversDao.getDiscoverObservable(any(LocationPointer.class))).thenReturn(discoversSubject);

        when(userPreferences.isUserLoggedIn()).thenReturn(true);
        when(userPreferences.getLocationObservable()).thenReturn(Observable.just(new UserLocation(0, 0, "zz", null, null, null, null)));

        presenter = new HomePresenter(shoutsDao, discoversDao, userPreferences, scheduler, context);
    }

    @Test
    public void testAfterStart_noErrorsOccured() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
    }

    @Test
    public void testAfterStart_correctAdapterItemsReturned() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        initData();

        assert_().that(subscriber.getOnNextEvents()).hasSize(2);
        final List<BaseAdapterItem> baseAdapterItems = subscriber.getOnNextEvents().get(1);
        assert_().that(baseAdapterItems.get(0)).isInstanceOf(HomePresenter.DiscoverHeaderAdapterItem.class);
        assert_().that(baseAdapterItems.get(1)).isInstanceOf(HomePresenter.DiscoverContainerAdapterItem.class);
        assert_().that(baseAdapterItems.get(2)).isInstanceOf(HomePresenter.ShoutHeaderAdapterItem.class);
        assert_().that(baseAdapterItems.get(3)).isInstanceOf(HomePresenter.ShoutAdapterItem.class);
    }

    @Test
    public void testAfterStart_correctSubAdapterItemsReturned() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        initData();
        final HomePresenter.DiscoverContainerAdapterItem discoverContainerAdapterItem =
                (HomePresenter.DiscoverContainerAdapterItem) subscriber.getOnNextEvents().get(1).get(1);

        assert_().that(discoverContainerAdapterItem.getAdapterItems()).isNotEmpty();
    }

    @Test
    public void testAfterDataArrived_progressIsHidden() throws Exception {
        final TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getProgressObservable().subscribe(subscriber);

        initData();

        subscriber.assertValueCount(2);
        subscriber.assertValues(false, false);
    }

    @Test
    public void testAfterDataArrived_errorIsNotShown() throws Exception {
        final TestSubscriber<Throwable> subscriber = new TestSubscriber<>();
        presenter.getErrorObservable().subscribe(subscriber);

        initData();

        subscriber.assertNoValues();
    }

    @Test
    public void testAfterErrorOccurs_errorIsShown() throws Exception {
        final TestSubscriber<Throwable> subscriber = new TestSubscriber<>();
        presenter.getErrorObservable().subscribe(subscriber);

        shoutsSubject.onError(new Throwable());

        subscriber.assertNoValues();
    }

    @Test
    public void testWhenNoShouts_ThenEmptyItemIsVisible() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        shoutsSubject.onNext(shoutsEmptyResponse());
        discoversSubject.onNext(discoverResponse());
        discoversDetailsSubject.onNext(discoverDetailsResponse());
        scheduler.triggerActions();

        assert_().that(subscriber.getOnNextEvents()).hasSize(2);
        final List<BaseAdapterItem> baseAdapterItems = subscriber.getOnNextEvents().get(1);
        assert_().that(baseAdapterItems.get(0)).isInstanceOf(HomePresenter.DiscoverHeaderAdapterItem.class);
        assert_().that(baseAdapterItems.get(1)).isInstanceOf(HomePresenter.DiscoverContainerAdapterItem.class);
        assert_().that(baseAdapterItems.get(2)).isInstanceOf(HomePresenter.ShoutHeaderAdapterItem.class);
        assert_().that(baseAdapterItems.get(3)).isInstanceOf(HomePresenter.ShoutsEmptyAdapterItem.class);
    }

    private void initData() {
        shoutsSubject.onNext(shoutsResponse());
        discoversSubject.onNext(discoverResponse());
        discoversDetailsSubject.onNext(discoverDetailsResponse());
        scheduler.triggerActions();
    }

    @Nonnull
    private ResponseOrError<DiscoverResponse> discoverResponse() {
        return ResponseOrError.fromData(new DiscoverResponse(1, "z", null, Lists.newArrayList(
                new Discover("1", null, null, null, 2, null, null))));
    }

    @Nonnull
    private ResponseOrError<DiscoverItemDetailsResponse> discoverDetailsResponse() {
        return ResponseOrError.fromData(new DiscoverItemDetailsResponse("2", true, false, Lists.<DiscoverChild>newArrayList(
                new DiscoverChild("id", null, null, null, null, null)), title, image));
    }

    @Nonnull
    private ResponseOrError<ShoutsResponse> shoutsResponse() {
        return ResponseOrError.fromData(new ShoutsResponse(1, "2", null, Lists.newArrayList(
                new Shout("id", null, null, null, null, null, null, 1f, 2f, null, null, null,
                        user, category, Lists.newArrayList(tag), 2))));
    }

    @Nonnull
    private ResponseOrError<ShoutsResponse> shoutsEmptyResponse() {
        return ResponseOrError.fromData(new ShoutsResponse(1, "2", null, null));
    }
}
