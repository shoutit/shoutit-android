package com.shoutit.app.android.view.signin.register;

import android.content.Context;
import android.location.Location;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.login.LoginUser;
import com.shoutit.app.android.view.signin.CoarseLocationObservableProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.observers.TestObserver;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegisterPresenterTest {

    private RegisterPresenter mRegisterPresenter;

    private BehaviorSubject<SignResponse> mResponseSubject;

    private BehaviorSubject<Location> mLocationObservable;

    @Mock
    Location location;

    @Mock
    ApiService mApiService;

    @Mock
    Context mContext;

    @Mock
    UserPreferences mUserPreferences;

    @Mock
    CoarseLocationObservableProvider coarseLocationProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(location.getLatitude()).thenReturn(1d);
        when(location.getLongitude()).thenReturn(1d);

        mResponseSubject = BehaviorSubject.create(new SignResponse("a", "b", "c", true, user));
        mLocationObservable = BehaviorSubject.create();

        when(mApiService.signup(any(EmailSignupRequest.class))).thenReturn(mResponseSubject);
        when(coarseLocationProvider.get(any(Context.class))).thenReturn(mLocationObservable);

        mRegisterPresenter = new RegisterPresenter(mApiService, mContext, coarseLocationProvider, mUserPreferences, Schedulers.immediate(), Schedulers.immediate());
    }

    @Test
    public void testRegisterSuccessful() {
        registerSuccessful();
    }

    @Test
    public void testRegisterSuccessfulAndTokenSet() {
        registerSuccessful();

        verify(mUserPreferences).setLoggedIn(anyString(), anyString());
    }

    private void registerSuccessful() {
        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);

        mRegisterPresenter.getEmailObserver().onNext("test");
        mRegisterPresenter.getPasswordObserver().onNext("testtest");
        mRegisterPresenter.getNameObserver().onNext("test");
        mRegisterPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnErrorEvents()).isEmpty();
        assert_().that(successObserver.getOnNextEvents()).hasSize(1);

        assert_().that(failObserver.getOnNextEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();
    }

    @Test
    public void testRegisterErrored() {
        mResponseSubject.onError(new RuntimeException());

        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        mRegisterPresenter.successObservable().subscribe(successObserver);
        mRegisterPresenter.failObservable().subscribe(failObserver);

        mRegisterPresenter.getEmailObserver().onNext("test");
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

        mRegisterPresenter.getEmailObserver().onNext("test");
        mRegisterPresenter.getPasswordObserver().onNext("testtest");
        mRegisterPresenter.getNameObserver().onNext("test");
        mRegisterPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnErrorEvents()).isEmpty();
        assert_().that(successObserver.getOnNextEvents()).hasSize(1);

        assert_().that(failObserver.getOnNextEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();

        final ArgumentCaptor<EmailSignupRequest> argumentCaptor = ArgumentCaptor.forClass(EmailSignupRequest.class);
        verify(mApiService).signup(argumentCaptor.capture());

        final LoginUser.Location location = argumentCaptor.getValue().getUser().getLocation();
        assert_().that(location.getLatitude()).isEqualTo(1d);
        assert_().that(location.getLongitude()).isEqualTo(1d);
    }
}