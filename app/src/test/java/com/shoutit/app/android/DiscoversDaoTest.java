package com.shoutit.app.android;

import com.appunite.rx.ResponseOrError;
import com.google.common.collect.Lists;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Discover;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.model.LocationPointer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DiscoversDaoTest {

    @Mock
    UserPreferences userPreferences;
    @Mock
    ApiService apiService;
    @Mock
    Shout shout;

    private LocationPointer locationPointer;

    private final TestScheduler scheduler = new TestScheduler();
    private DiscoversDao discoversDao;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        locationPointer = new LocationPointer("GE", "Berlin");

        when(userPreferences.getUserCountryCode()).thenReturn("GE");
        when(apiService.discovers(anyString(), anyInt(), anyInt()))
                .thenReturn(Observable.just(discoversResponse()));
        when(apiService.discoverItem(anyString()))
                .thenReturn(Observable.just(discoverItemDetailsResponse()));

        discoversDao = new DiscoversDao(apiService, scheduler);
    }

    @Test
    public void testDiscoverRequest() {
        final TestSubscriber<ResponseOrError<DiscoverResponse>> subscriber = new TestSubscriber<>();

        discoversDao.getDiscoverObservable(locationPointer)
                .subscribe(subscriber);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }

    @Test
    public void testDiscoverItemRequest() {
        final TestSubscriber<ResponseOrError<DiscoverItemDetailsResponse>> subscriber = new TestSubscriber<>();

        discoversDao.getDiscoverItemDao("z").getDiscoverItemObservable()
                .subscribe(subscriber);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }

    private DiscoverResponse discoversResponse() {
        return new DiscoverResponse(1, "z", null,
                Lists.newArrayList(new Discover("z", null, null, null, 1, null, null)));
    }

    private DiscoverItemDetailsResponse discoverItemDetailsResponse() {
        return new DiscoverItemDetailsResponse("z", true, false,
                Lists.newArrayList(new DiscoverChild("z", null, null, null, null, null)), title, image);
    }
}
