package com.shoutit.app.android.view.location;

import android.content.Context;
import android.database.CursorWindow;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.android.gms.common.api.Batch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.internal.AutocompletePredictionEntity;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.utils.LocationUtils;
import com.shoutit.app.android.utils.PermissionHelper;

import org.hamcrest.internal.ArrayIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {LocationUtils.class, PermissionHelper.class, Status.class})
public class LocationPresenterTest {

    private LocationPresenter presenter;
    private final TestScheduler testScheduler = new TestScheduler();

    @Mock
    GoogleApiClient googleApiClient;
    @Mock
    ApiService apiService;
    @Mock
    Context context;
    @Mock
    UserPreferences userPreferences;
    @Mock
    AutocompletePrediction autocompletePrediction;
    @Mock
    android.location.Location gpsLocation;
    @Mock
    Status status;

    // TODO Tempory commented - some problems with mocking google stuff...
  /*  @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(LocationUtils.class);
        PowerMockito.mockStatic(PermissionHelper.class);
        @SuppressWarnings("unchecked") final PendingResult<AutocompletePredictionBuffer> pendingResult =
                (PendingResult<AutocompletePredictionBuffer>) Mockito.mock(PendingResult.class, Mockito.RETURNS_MOCKS);
        final Iterator mockIterator = Mockito.mock(Iterator.class, Mockito.RETURNS_MOCKS);
        final AutocompletePredictionBuffer predictionBuffer = Mockito.mock(AutocompletePredictionBuffer.class, Mockito.RETURNS_MOCKS);

        when(apiService.geocode(anyString()))
                .thenReturn(Observable.just(UserLocation.withCoordinates(0, 0)));
        when(userPreferences.automaticLocationTrackingEnabled())
                .thenReturn(true);
        when(userPreferences.getLocationObservable()).thenReturn(Observable.just(new UserLocation(0, 1, "c", null, null, null, null)));
        when(googleApiClient.isConnected()).thenReturn(true);
        when(PermissionHelper.hasPermission(any(Context.class), anyString())).thenReturn(true);

        when(gpsLocation.getLatitude()).thenReturn(1d);
        when(gpsLocation.getLongitude()).thenReturn(2d);

        when(pendingResult.await()).thenReturn(predictionBuffer);
        when(LocationUtils.getPredictionsForQuery(any(GoogleApiClient.class), anyString())).thenReturn(pendingResult);
        when(predictionBuffer.getStatus()).thenReturn(status);
        when(status.isSuccess()).thenReturn(true);

        final ArrayList<AutocompletePrediction> predictionLists = Lists.newArrayList(autocompletePrediction);
        when(mockIterator.hasNext()).thenReturn(true, false, false, false);
        when(mockIterator.next()).thenReturn(predictionLists);
        when(autocompletePrediction.getPlaceId()).thenReturn("id");
        when(autocompletePrediction.getFullText(null)).thenReturn("lala");

        presenter = new LocationPresenter(googleApiClient, testScheduler, testScheduler,
                context, apiService, userPreferences);
    }

    @Test
    public void testName() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        presenter.getQuerySubject().onNext("Berlin");
        presenter.getLastGpsLocationObserver().onNext(gpsLocation);
        testScheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
        subscriber.assertValueCount(6);
        final List<BaseAdapterItem> lastEvent = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(lastEvent.get(0)).isInstanceOf(LocationPresenter.CurrentLocationAdapterItem.class);
        assert_().that(lastEvent.get(1)).isInstanceOf(LocationPresenter.CurrentLocationAdapterItem.class);
        assert_().that(lastEvent.get(2)).isInstanceOf(LocationPresenter.PlaceAdapterItem.class);
    }*/
}
