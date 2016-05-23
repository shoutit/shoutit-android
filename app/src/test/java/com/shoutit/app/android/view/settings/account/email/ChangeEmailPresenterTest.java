package com.shoutit.app.android.view.settings.account.email;

import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UpdateUserRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.Validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { Validators.class})
public class ChangeEmailPresenterTest {

    @Mock
    ApiService apiService;
    @Mock
    UserPreferences userPreferences;

    private ChangeEmailPresenter presenter;
    private final TestScheduler scheduler = new TestScheduler();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Validators.class);

        when(apiService.updateUser(any(UpdateUserRequest.class)))
                .thenReturn(Observable.just(new User("z", null, null, null, null, null, null, null, false, null,
                null, false, false, false, null, 1, null, null, null, 1, null, false, null, null, null, null, null, null)));
        when(Validators.isEmailValid(anyString())).thenReturn(true);

        presenter = new ChangeEmailPresenter(apiService, scheduler, scheduler, userPreferences);
    }

    @Test
    public void testWhenEmailValid_success() throws Exception {
        TestSubscriber<Object> subscriber = new TestSubscriber<>();
        presenter.getSuccessObservable().subscribe(subscriber);

        presenter.getEmailObserver().onNext("email@123.com");
        presenter.getConfirmClickSubject().onNext(null);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }

    @Test
    public void testWhenSuccess_correctProgressSequence() throws Exception {
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getProgressObservable().subscribe(subscriber);

        presenter.getEmailObserver().onNext("email@123.com");
        presenter.getConfirmClickSubject().onNext(null);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(3);
        subscriber.assertValues(false, true, false);
    }

    @Test
    public void testWhenError_correctProgressSequence() throws Exception {
        when(apiService.updateUser(any(UpdateUserRequest.class)))
                .thenReturn(Observable.<User>error(new Throwable("")));
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getProgressObservable().subscribe(subscriber);

        presenter.getEmailObserver().onNext("email@123.com");
        presenter.getConfirmClickSubject().onNext(null);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(3);
        subscriber.assertValues(false, true, false);
    }

    @Test
    public void testWhenEmailInvalid_error() throws Exception {
        when(Validators.isEmailValid(anyString())).thenReturn(false);
        TestSubscriber<Object> subscriber = new TestSubscriber<>();
        presenter.getWrongEmailErrorObservable().subscribe(subscriber);

        presenter.getEmailObserver().onNext("email@123.com");
        presenter.getConfirmClickSubject().onNext(null);
        scheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }
}
