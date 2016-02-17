package com.shoutit.app.android.view.discover;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Discover;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.model.LocationPointer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DiscoverPresenterTest {

    @Mock
    UserPreferences userPreferences;
    @Mock
    DiscoversDao discoversDao;
    @Mock
    DiscoverShoutsDao discoverShoutsDao;
    @Mock
    ApiService apiService;
    @Mock
    Scheduler networkScheduler;
    @Mock
    DiscoversDao.DiscoverItemDao discoverItemDao;

    private final PublishSubject<ResponseOrError<DiscoverItemDetailsResponse>> discoverItemSubject = PublishSubject.create();
    private DiscoverPresenter presenter;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(userPreferences.getLocationObservable())
                .thenReturn(Observable.just(new UserLocation(0, 0, "z", null, null, null, null)));

        when(discoversDao.getDiscoverObservable(any(LocationPointer.class)))
                .thenReturn(Observable.just(ResponseOrError.fromData(getDiscoverResponse())));
        when(discoversDao.getDiscoverItemDao(anyString())).thenReturn(discoverItemDao);
        when(discoverItemDao.getDiscoverItemObservable()).thenReturn(discoverItemSubject);
        when(discoverShoutsDao.getShoutsObservable(anyString()))
                .thenReturn(Observable.just(ResponseOrError.fromData(getShoutsResponse())));

        presenter = new DiscoverPresenter(userPreferences, discoversDao,
                discoverShoutsDao, Optional.<String>absent(), Schedulers.immediate());
    }

    @Test
    public void testOnSubscribedWithShowChildrenAndShowShouts_correctItemsReturned() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        discoverItemSubject.onNext(getDiscoverItemResponse(true, true));

        subscriber.assertNoErrors();
        final List<BaseAdapterItem> items = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(items.get(0)).isInstanceOf(DiscoverPresenter.HeaderAdapterItem.class);
        assert_().that(items.get(1)).isInstanceOf(DiscoverPresenter.DiscoverAdapterItem.class);
        assert_().that(items.get(2)).isInstanceOf(DiscoverPresenter.ShoutHeaderAdapterItem.class);
        assert_().that(items.get(3)).isInstanceOf(DiscoverPresenter.ShoutAdapterItem.class);
        assert_().that(items.get(4)).isInstanceOf(DiscoverPresenter.ShowMoreButtonAdapterItem.class);
    }

    @Test
    public void testOnSubscribedWithShowShoutsOnly_correctItemsReturned() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        discoverItemSubject.onNext(getDiscoverItemResponse(false, true));

        subscriber.assertNoErrors();
        final List<BaseAdapterItem> items = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(items.get(0)).isInstanceOf(DiscoverPresenter.HeaderAdapterItem.class);
        assert_().that(items.get(1)).isInstanceOf(DiscoverPresenter.ShoutHeaderAdapterItem.class);
        assert_().that(items.get(2)).isInstanceOf(DiscoverPresenter.ShoutAdapterItem.class);
        assert_().that(items.get(3)).isInstanceOf(DiscoverPresenter.ShowMoreButtonAdapterItem.class);
    }

    @Test
    public void testOnSubscribedWithShowChildOnly_correctItemsReturned() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        discoverItemSubject.onNext(getDiscoverItemResponse(true, false));

        subscriber.assertNoErrors();
        final List<BaseAdapterItem> items = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(items.get(0)).isInstanceOf(DiscoverPresenter.HeaderAdapterItem.class);
        assert_().that(items.get(1)).isInstanceOf(DiscoverPresenter.DiscoverAdapterItem.class);
    }

    @Test
    public void testOnSubscribedWithNoShoutsAndNoChilds_correctItemsReturned() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        discoverItemSubject.onNext(getDiscoverItemResponse(false, false));

        subscriber.assertNoErrors();
        final List<BaseAdapterItem> items = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(items.get(0)).isInstanceOf(DiscoverPresenter.HeaderAdapterItem.class);
    }

    @Test
    public void testOnDataDisplayed_progressNotVisible() throws Exception {
        final TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getProgressObservable().subscribe(subscriber);

        discoverItemSubject.onNext(getDiscoverItemResponse(true, true));

        subscriber.assertValues(false, false, false);
    }

    @Test
    public void testAfterDataArrived_errorIsNotShown() throws Exception {
        final TestSubscriber<Throwable> subscriber = new TestSubscriber<>();
        presenter.getErrorsObservable().subscribe(subscriber);

        discoverItemSubject.onNext(getDiscoverItemResponse(true, true));

        subscriber.assertNoValues();
    }

    @Test
    public void testAfterErrorOccurs_errorIsShown() throws Exception {
        final TestSubscriber<Throwable> subscriber = new TestSubscriber<>();
        presenter.getErrorsObservable().subscribe(subscriber);

        discoverItemSubject.onNext(ResponseOrError.<DiscoverItemDetailsResponse>fromError(new Throwable("z")));

        subscriber.assertValueCount(1);
    }

    private ShoutsResponse getShoutsResponse() {
        return new ShoutsResponse(0, "a", null,
                Lists.newArrayList(new Shout("z", null, null, null, null, null, null, 0, 0, null, null, null, null, null, null, 1L)));
    }

    private DiscoverResponse getDiscoverResponse() {
        return new DiscoverResponse(1, "z", null,
                Lists.newArrayList(new Discover("id", null, null, null, 1, null, null)));
    }

    public ResponseOrError<DiscoverItemDetailsResponse> getDiscoverItemResponse(boolean showChildren, boolean showShouts) {
        return ResponseOrError.fromData(new DiscoverItemDetailsResponse("id", showChildren, showShouts,
                Lists.newArrayList(new DiscoverChild("id", null, null, null, null, null)), null, null));
    }
}
