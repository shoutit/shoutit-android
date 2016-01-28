package com.shoutit.app.android.view.signin.login;

import android.content.Context;
import android.location.Location;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;
import com.shoutit.app.android.view.signin.CoarseLocationObservableProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestObserver;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginPresenterTest {

    private LoginPresenter mLoginPresenter;

    private BehaviorSubject<SignResponse> mResponseSubject;

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

        mResponseSubject = BehaviorSubject.create(new SignResponse("a", "b", "c", true));
        when(mApiService.login(any(EmailLoginRequest.class))).thenReturn(mResponseSubject);
        when(coarseLocationProvider.get(any(Context.class))).thenReturn(Observable.just((Location) null));

        mLoginPresenter = new LoginPresenter(mApiService, mContext, coarseLocationProvider, mUserPreferences, Schedulers.immediate(), Schedulers.immediate());
    }

    @Test
    public void testLoginSuccessful() {
        loginSuccessful();
    }

    @Test
    public void testLoginSuccessfulAndTokenSet() {
        loginSuccessful();

        verify(mUserPreferences).setLoggedIn(anyString(), anyString());
    }

    private void loginSuccessful() {
        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        mLoginPresenter.successObservable().subscribe(successObserver);
        mLoginPresenter.failObservable().subscribe(failObserver);

        mLoginPresenter.getEmailObserver().onNext("test");
        mLoginPresenter.getPasswordObserver().onNext("test");
        mLoginPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnErrorEvents()).isEmpty();
        assert_().that(successObserver.getOnNextEvents()).hasSize(1);

        assert_().that(failObserver.getOnNextEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();
    }

    @Test
    public void testLoginErrored() {
        mResponseSubject.onError(new RuntimeException());

        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        mLoginPresenter.successObservable().subscribe(successObserver);
        mLoginPresenter.failObservable().subscribe(failObserver);

        mLoginPresenter.getEmailObserver().onNext("test");
        mLoginPresenter.getPasswordObserver().onNext("test");
        mLoginPresenter.getProceedObserver().onNext(new Object());

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
        mLoginPresenter.successObservable().subscribe(successObserver);
        mLoginPresenter.failObservable().subscribe(failObserver);
        mLoginPresenter.getPasswordEmpty().subscribe(emptyPasswordObserver);

        mLoginPresenter.getEmailObserver().onNext("test");
        mLoginPresenter.getPasswordObserver().onNext("");
        mLoginPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnNextEvents()).isEmpty();
        assert_().that(successObserver.getOnErrorEvents()).isEmpty();

        assert_().that(failObserver.getOnErrorEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();

        assert_().that(emptyPasswordObserver.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testWhenEmptyMail_showError() {
        final TestObserver<Object> successObserver = new TestObserver<>();
        final TestObserver<Object> failObserver = new TestObserver<>();
        final TestObserver<Object> emptyEmailObserver = new TestObserver<>();
        mLoginPresenter.successObservable().subscribe(successObserver);
        mLoginPresenter.failObservable().subscribe(failObserver);
        mLoginPresenter.getEmailEmpty().subscribe(emptyEmailObserver);

        mLoginPresenter.getEmailObserver().onNext("");
        mLoginPresenter.getPasswordObserver().onNext("test");
        mLoginPresenter.getProceedObserver().onNext(new Object());

        assert_().that(successObserver.getOnNextEvents()).isEmpty();
        assert_().that(successObserver.getOnErrorEvents()).isEmpty();

        assert_().that(failObserver.getOnErrorEvents()).isEmpty();
        assert_().that(failObserver.getOnNextEvents()).isEmpty();

        assert_().that(emptyEmailObserver.getOnNextEvents()).hasSize(1);
    }

}