package com.shoutit.app.android.view.signin.register;

import android.content.Context;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UserLocationSimple;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.Validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import rx.Observable;
import rx.observers.TestObserver;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Validators.class)
public class RegisterPresenterTest {

    private RegisterPresenter mRegisterPresenter;

    private BehaviorSubject<SignResponse> mResponseSubject;

    private BehaviorSubject<UserLocation> mLocationObservable;

    @Mock
    UserLocation location;

    @Mock
    ApiService mApiService;

    @Mock
    Context mContext;

    @Mock
    UserPreferences mUserPreferences;

    @Mock
    User user;

    @Mock
    LocationManager locationManager;

    @Mock
    MixPanel mixPanel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Validators.class);

        when(location.getLatitude()).thenReturn(1d);
        when(location.getLongitude()).thenReturn(1d);

        mResponseSubject = BehaviorSubject.create(new SignResponse("a", "b", "c", true, user));
        mLocationObservable = BehaviorSubject.create();

        when(mApiService.signup(any(EmailSignupRequest.class))).thenReturn(mResponseSubject);
        when(locationManager.updateUserLocationObservable()).thenReturn(mLocationObservable);

        when(mUserPreferences.getLocationObservable())
                .thenReturn(Observable.just(location));

        when(Validators.isEmailValid(anyString()))
                .thenReturn(true);

        when(mixPanel.getDistinctId())
                .thenReturn("id");

        mRegisterPresenter = new RegisterPresenter(mApiService,
                mUserPreferences, Schedulers.immediate(), Schedulers.immediate(), mixPanel);
    }

    @Test
    public void testRegisterSuccessful() {
        registerSuccessful();
    }

    @Test
    public void testRegisterSuccessfulAndTokenSet() {
        registerSuccessful();

        verify(mUserPreferences).setLoggedIn(anyString(), anyString(), any(User.class));
    }

    private void registerSuccessful() {
        final TestSubscriber<Object> successObserver = new TestSubscriber<>();
        final TestSubscriber<Object> failObserver = new TestSubscriber<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);

        mRegisterPresenter.getEmailObserver().onNext("test@test.com");
        mRegisterPresenter.getPasswordObserver().onNext("testtest");
        mRegisterPresenter.getNameObserver().onNext("test");
        mRegisterPresenter.getProceedObserver().onNext(new Object());

        successObserver.assertNoErrors();
        successObserver.assertValueCount(1);

        failObserver.assertNoErrors();
        failObserver.assertNoValues();
    }

    @Test
    public void testRegisterErrored() {
        mResponseSubject.onError(new RuntimeException());

        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);

        mRegisterPresenter.getEmailObserver().onNext("test@z.com");
        mRegisterPresenter.getPasswordObserver().onNext("testtest");
        mRegisterPresenter.getNameObserver().onNext("test");
        mRegisterPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnNextEvents()).isEmpty();
        assert_().that(successObserver.getOnErrorEvents()).isEmpty();

        assert_().that(failObserver.getOnErrorEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testWhenEmptyPassword_showError() {
        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        final TestObserver<Object> emptyPasswordObserver = new TestObserver<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);
        mRegisterPresenter.getPasswordEmpty().subscribe(emptyPasswordObserver);

        mRegisterPresenter.getEmailObserver().onNext("test");
        mRegisterPresenter.getPasswordObserver().onNext("");
        mRegisterPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnNextEvents()).isEmpty();
        assert_().that(successObserver.getOnErrorEvents()).isEmpty();

        assert_().that(failObserver.getOnErrorEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();

        assert_().that(emptyPasswordObserver.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testWhenTooLongPassword_showError() {
        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        final TestObserver<Object> emptyPasswordObserver = new TestObserver<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);
        mRegisterPresenter.getPasswordEmpty().subscribe(emptyPasswordObserver);

        mRegisterPresenter.getEmailObserver().onNext("test");
        mRegisterPresenter.getPasswordObserver().onNext("123456789012345678901234");
        mRegisterPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnNextEvents()).isEmpty();
        assert_().that(successObserver.getOnErrorEvents()).isEmpty();

        assert_().that(failObserver.getOnErrorEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();

        assert_().that(emptyPasswordObserver.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testWhenEmptyPasswordAndButtonNotClicked_dontShowError() {
        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        final TestObserver<Object> emptyPasswordObserver = new TestObserver<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);
        mRegisterPresenter.getPasswordEmpty().subscribe(emptyPasswordObserver);

        mRegisterPresenter.getEmailObserver().onNext("test");
        mRegisterPresenter.getPasswordObserver().onNext("");

        assert_().that(successObserver.getOnNextEvents()).isEmpty();
        assert_().that(successObserver.getOnErrorEvents()).isEmpty();

        assert_().that(failObserver.getOnErrorEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();

        assert_().that(emptyPasswordObserver.getOnNextEvents()).hasSize(0);
    }

    @Test
    public void testWhenEmptyMail_showError() {
        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        final TestObserver<Object> emptyEmailObserver = new TestObserver<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);
        mRegisterPresenter.getEmailEmpty().subscribe(emptyEmailObserver);

        mRegisterPresenter.getEmailObserver().onNext("");
        mRegisterPresenter.getPasswordObserver().onNext("test");
        mRegisterPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnNextEvents()).isEmpty();
        assert_().that(successObserver.getOnErrorEvents()).isEmpty();

        assert_().that(failObserver.getOnErrorEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();

        assert_().that(emptyEmailObserver.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testWhenEmptyName_showError() {
        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        final TestObserver<Object> emptyNameObserver = new TestObserver<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);
        mRegisterPresenter.getNameEmpty().subscribe(emptyNameObserver);

        mRegisterPresenter.getEmailObserver().onNext("test");
        mRegisterPresenter.getPasswordObserver().onNext("test");
        mRegisterPresenter.getNameObserver().onNext("");
        mRegisterPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnNextEvents()).isEmpty();
        assert_().that(successObserver.getOnErrorEvents()).isEmpty();

        assert_().that(failObserver.getOnErrorEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();

        assert_().that(emptyNameObserver.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testWhenNewLocationIsPassed_locationPassedToRequest() {
        mRegisterPresenter.getLocationObservable().subscribe();
        mLocationObservable.onNext(location);

        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);

        mRegisterPresenter.getEmailObserver().onNext("test@z.com");
        mRegisterPresenter.getPasswordObserver().onNext("testtest");
        mRegisterPresenter.getNameObserver().onNext("test");
        mRegisterPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnErrorEvents()).isEmpty();
        assert_().that(successObserver.getOnNextEvents()).hasSize(1);

        assert_().that(failObserver.getOnNextEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();

        final ArgumentCaptor<EmailSignupRequest> argumentCaptor = ArgumentCaptor.forClass(EmailSignupRequest.class);
        verify(mApiService).signup(argumentCaptor.capture());

        final UserLocationSimple location = argumentCaptor.getValue().getProfile().getLocation();
        assert_().that(location.getLatitude()).isEqualTo(1d);
        assert_().that(location.getLongitude()).isEqualTo(1d);
    }
}