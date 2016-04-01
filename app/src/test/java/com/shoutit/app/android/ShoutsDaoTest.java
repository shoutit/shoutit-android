package com.shoutit.app.android;

import com.appunite.rx.ResponseOrError;
import com.google.common.collect.Lists;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.model.LocationPointer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class ShoutsDaoTest {

    @Mock
    UserPreferences userPreferences;
    @Mock
    ApiService apiService;
    @Mock
    Shout shout;

    private final TestScheduler scheduler = new TestScheduler();
    private ShoutsDao shoutsDao;
    private LocationPointer locationPointer;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        locationPointer = new LocationPointer("GE", "Berlin");

        when(userPreferences.isUserLoggedIn()).thenReturn(true);
        when(apiService.shoutsForLocation(anyString(), anyString(),anyString(), anyInt(), anyInt()))
                .thenReturn(Observable.just(shoutsResponse()));
        when(apiService.home(anyString(), anyInt(), anyInt()))
                .thenReturn(Observable.just(shoutsResponse()));

        shoutsDao = new ShoutsDao(apiService, scheduler, userPreferences);
    }

    @Test
    public void testShoutsRequestForUser() throws Exception {
        final TestSubscriber<ResponseOrError<ShoutsResponse>> subscriber = new TestSubscriber<>();
        when(userPreferences.isUserLoggedIn()).thenReturn(true);

        shoutsDao.getHomeShoutsObservable(locationPointer).subscribe(subscriber);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }

    @Test
    public void testShoutsRequestForGuest() throws Exception {
        final TestSubscriber<ResponseOrError<ShoutsResponse>> subscriber = new TestSubscriber<>();
        when(userPreferences.isUserLoggedIn()).thenReturn(false);

        shoutsDao.getHomeShoutsObservable(locationPointer).subscribe(subscriber);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }

    @Test
    public void testLoadMoreShoutsForUser() throws Exception {
        final TestSubscriber<ResponseOrError<ShoutsResponse>> subscriber = new TestSubscriber<>();
        when(userPreferences.isUserLoggedIn()).thenReturn(true);

        shoutsDao.getHomeShoutsObservable(locationPointer).subscribe(subscriber);
        scheduler.triggerActions();
        shoutsDao.getLoadMoreHomeShoutsObserver(locationPointer).onNext(null);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(2);
        assert_().that(subscriber.getOnNextEvents().get(1).data().getShouts()).hasSize(2);
    }


    @Test
    public void testLoadMoreShoutsForGuest() throws Exception {
        final TestSubscriber<ResponseOrError<ShoutsResponse>> subscriber = new TestSubscriber<>();
        when(userPreferences.isUserLoggedIn()).thenReturn(false);

        shoutsDao.getHomeShoutsObservable(locationPointer).subscribe(subscriber);
        scheduler.triggerActions();
        shoutsDao.getLoadMoreHomeShoutsObserver(locationPointer).onNext(null);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(2);
        assert_().that(subscriber.getOnNextEvents().get(1).data().getShouts()).hasSize(2);
    }

    private ShoutsResponse shoutsResponse() {
        return new ShoutsResponse(1, "z", null, Lists.newArrayList(shout));
    }
}
