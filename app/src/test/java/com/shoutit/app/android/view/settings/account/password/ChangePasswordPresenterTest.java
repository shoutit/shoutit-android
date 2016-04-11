package com.shoutit.app.android.view.settings.account.password;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ChangePasswordRequest;
import com.shoutit.app.android.api.model.User;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ChangePasswordPresenterTest {

    @Mock
    ApiService apiService;
    @Mock
    UserPreferences userPreferences;

    private ChangePasswordPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(apiService.changePassword(any(ChangePasswordRequest.class)))
                .thenReturn(Observable.just(ResponseBody.create(null, "z")));

        when(userPreferences.getUserObservable())
                .thenReturn(Observable.just(new User("z", null, null, null, null, null, null, null, false, null,
                null, false, false, false, null, 1, null, null, null, 1, null, false, null, null, null, null, null)));

        presenter = new ChangePasswordPresenter(apiService, userPreferences, Schedulers.immediate(), Schedulers.immediate());
    }

    @Test
    public void testWhenDataCorrect_success() throws Exception {
        TestSubscriber<Object> successSubscriber = new TestSubscriber<>();
        presenter.getRequestSuccessObservable().subscribe(successSubscriber);

        presenter.getOldPasswordObserver().onNext("Password");
        presenter.getPasswordObserver().onNext("Password");
        presenter.getPasswordConfirmObserver().onNext("Password");
        presenter.getConfirmClickObserver().onNext(null);

        successSubscriber.assertNoErrors();
        successSubscriber.assertValueCount(1);
    }


    @Test
    public void testWhenDataCorrectWithNoOldPassword_success() throws Exception {
        TestSubscriber<Object> subscriber = new TestSubscriber<>();
        presenter.getRequestSuccessObservable().subscribe(subscriber);

        presenter.getPasswordObserver().onNext("Password");
        presenter.getPasswordConfirmObserver().onNext("Password");
        presenter.getConfirmClickObserver().onNext(null);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }

    @Test
    public void testWhenVerificationPasswordEmpty_errorOccurs() throws Exception {
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getPasswordConfirmError().subscribe(subscriber);
        TestSubscriber<Object> successSubscriber = new TestSubscriber<>();
        presenter.getRequestSuccessObservable().subscribe(successSubscriber);

        presenter.getPasswordObserver().onNext("Password");
        presenter.getConfirmClickObserver().onNext(null);

        subscriber.assertNoErrors();
        subscriber.assertValues(true);
        successSubscriber.assertValueCount(0);
    }

    @Test
    public void testWhenPasswordConfirmEmpty_errorOccurs() throws Exception {
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getPasswordConfirmError().subscribe(subscriber);

        TestSubscriber<Object> successSubscriber = new TestSubscriber<>();
        presenter.getRequestSuccessObservable().subscribe(successSubscriber);

        presenter.getPasswordObserver().onNext("Password");
        presenter.getConfirmClickObserver().onNext(null);

        subscriber.assertNoErrors();
        subscriber.assertValues(true);
        successSubscriber.assertValueCount(0);
    }

    @Test
    public void testWhenPasswordLessThan6_errorOccurs() throws Exception {
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getPasswordError().subscribe(subscriber);

        TestSubscriber<Object> successSubscriber = new TestSubscriber<>();
        presenter.getRequestSuccessObservable().subscribe(successSubscriber);

        presenter.getPasswordObserver().onNext("Pass");
        presenter.getPasswordConfirmObserver().onNext("Password");
        presenter.getConfirmClickObserver().onNext(null);

        subscriber.assertNoErrors();
        subscriber.assertValues(true);
        successSubscriber.assertValueCount(0);
    }

    @Test
    public void testWhenPasswordConfirmLessThan6_errorOccurs() throws Exception {
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getPasswordConfirmError().subscribe(subscriber);

        TestSubscriber<Object> successSubscriber = new TestSubscriber<>();
        presenter.getRequestSuccessObservable().subscribe(successSubscriber);

        presenter.getPasswordObserver().onNext("Password");
        presenter.getPasswordConfirmObserver().onNext("Pass");
        presenter.getConfirmClickObserver().onNext(null);

        subscriber.assertNoErrors();
        subscriber.assertValues(true);
        successSubscriber.assertValueCount(0);
    }

    @Test
    public void testWhenOldPasswordNotSetAndDataCorrect_success() throws Exception {
        TestSubscriber<Object> subscriber = new TestSubscriber<>();
        presenter.getRequestSuccessObservable().subscribe(subscriber);

        when(userPreferences.getUserObservable())
                .thenReturn(Observable.just(userWithPasswordSet()));

        presenter.getOldPasswordObserver().onNext("Password");
        presenter.getPasswordObserver().onNext("Password");
        presenter.getPasswordConfirmObserver().onNext("Password");
        presenter.getConfirmClickObserver().onNext(null);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }

    private User userWithPasswordSet() {
        return new User("z", null, null, null, null, null, null, null, false, null,
                null, false, false, false, null, 1, null, null, null, 1, null, false, null, null, null, null, null);
    }
}
