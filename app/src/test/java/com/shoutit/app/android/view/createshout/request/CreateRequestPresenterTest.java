package com.shoutit.app.android.view.createshout.request;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.AppPreferences;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CreateRequestShoutWithPriceRequest;
import com.shoutit.app.android.api.model.CreateShoutResponse;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.facebook.FacebookHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ResourcesHelper.class})
public class CreateRequestPresenterTest {

    public abstract class StubListener implements CreateRequestPresenter.Listener {


    }

    private CreateRequestPresenter mCreateRequestPresenter;

    @Mock
    StubListener mListener;

    @Mock
    Context mContext;

    @Mock
    UserPreferences mUserPreferences;

    @Mock
    FacebookHelper facebookHelper;

    ShoutsGlobalRefreshPresenter globalRefreshPresenter;

    @Mock
    ApiService mApiService;

    @Mock
    AppPreferences appPreferences;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(ResourcesHelper.class);

        globalRefreshPresenter = new ShoutsGlobalRefreshPresenter();
        when(mApiService.getCurrencies()).thenReturn(Observable.just(currencyList()));
        when(ResourcesHelper.getResourceIdForName(anyString(), any(Context.class))).thenReturn(1);
        when(mUserPreferences.getLocationObservable()).thenReturn(Observable.just(getUserLocation()));
        when(mListener.getRequestData()).thenReturn(new CreateRequestPresenter.RequestData("123456", "5", "a"));

        mCreateRequestPresenter = new CreateRequestPresenter(mUserPreferences, mContext, mApiService,
                Schedulers.immediate(), Schedulers.immediate(), globalRefreshPresenter, facebookHelper, appPreferences);
    }

    @NonNull
    private UserLocation getUserLocation() {
        return new UserLocation(1, 1, "", "", "", "", "");
    }

    private CreateShoutResponse emptyCreateShoutResponse() {
        return new CreateShoutResponse("", "", "");
    }

    @NonNull
    private List<Currency> currencyList() {
        return ImmutableList.of(new Currency("a", "b", "c"));
    }

    @Test
    public void testWhenButtonClicked_requestDataFromView() {
        when(mListener.getRequestData()).thenReturn(new CreateRequestPresenter.RequestData("", "", ""));
        when(mApiService.createShoutRequest(any(CreateRequestShoutWithPriceRequest.class))).thenReturn(Observable.just(emptyCreateShoutResponse()));
        mCreateRequestPresenter.registerListener(mListener);

        mCreateRequestPresenter.confirmClicked(true);

        verify(mListener).getRequestData();
    }

    @Test
    public void testWhenButtonClicked_DataSentToApi() {
        when(mListener.getRequestData()).thenReturn(new CreateRequestPresenter.RequestData("123456", "5", "z"));
        when(mApiService.createShoutRequest(any(CreateRequestShoutWithPriceRequest.class))).thenReturn(Observable.just(emptyCreateShoutResponse()));
        mCreateRequestPresenter.registerListener(mListener);

        mCreateRequestPresenter.confirmClicked(true);

        verify(mApiService).createShoutRequest(any(CreateRequestShoutWithPriceRequest.class));
    }

    @Test
    public void testWhenButtonClickedAndRequestSuccessful_progressShownAndHidden() {
        when(mListener.getRequestData()).thenReturn(new CreateRequestPresenter.RequestData("123456", "5", "z"));
        when(mApiService.createShoutRequest(any(CreateRequestShoutWithPriceRequest.class))).thenReturn(Observable.just(emptyCreateShoutResponse()));
        mCreateRequestPresenter.registerListener(mListener);

        mCreateRequestPresenter.confirmClicked(true);

        verify(mListener, times(2)).showProgress();
        verify(mListener, times(2)).hideProgress();
    }

    @Test
    public void testWhenButtonClickedAndRequestFailed_progressShownAndHiddenAndErrorShown() {
        when(mListener.getRequestData()).thenReturn(new CreateRequestPresenter.RequestData("123456", "5", "z"));

        when(mApiService.createShoutRequest(any(CreateRequestShoutWithPriceRequest.class))).thenReturn(Observable.<CreateShoutResponse>error(new RuntimeException("")));
        mCreateRequestPresenter.registerListener(mListener);

        mCreateRequestPresenter.confirmClicked(true);

        verify(mListener, times(2)).showProgress();
        verify(mListener, times(2)).hideProgress();
        verify(mListener).showApiError(any(Throwable.class));
    }

    @Test
    public void whenRegistered_locationWillBeSet() {
        mCreateRequestPresenter.registerListener(mListener);

        verify(mListener).setLocation(anyInt(), anyString());
    }

    @Test
    public void whenRegisteredAndCurrenciesSuccesfull_currenciesSet() {
        mCreateRequestPresenter.registerListener(mListener);

        verify(mListener).setCurrenciesEnabled(false);
        verify(mListener).showProgress();
        verify(mListener).hideProgress();
        verify(mListener).setCurrencies(anyList());
        verify(mListener).removeRetryCurrenciesListener();
    }

    @Test
    public void whenRegisteredAndCurrenciesError_errorShown() {
        when(mApiService.getCurrencies()).thenReturn(Observable.<List<Currency>>error(new RuntimeException()));
        mCreateRequestPresenter.registerListener(mListener);

        verify(mListener).setCurrenciesEnabled(false);
        verify(mListener).showProgress();
        verify(mListener).hideProgress();
        verify(mListener).showCurrenciesError();
        verify(mListener).setRetryCurrenciesListener();
    }

    @Test
    public void whenRetryCurrenciesClicked_currenciesFetchedAgain() {
        when(mApiService.getCurrencies()).thenReturn(Observable.<List<Currency>>error(new RuntimeException()));
        mCreateRequestPresenter.registerListener(mListener);

        when(mApiService.getCurrencies()).thenReturn(Observable.just(currencyList()));
        mCreateRequestPresenter.retryCurrencies();
        verify(mListener).setCurrencies(anyList());
    }

    @Test
    public void whenChangedLocation_viewIsUpdate() {
        mCreateRequestPresenter.registerListener(mListener);

        mCreateRequestPresenter.updateLocation(getUserLocation());

        verify(mListener, times(2)).setLocation(anyInt(), anyString());
    }
}