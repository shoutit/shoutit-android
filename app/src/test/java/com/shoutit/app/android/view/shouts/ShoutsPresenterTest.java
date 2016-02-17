package com.shoutit.app.android.view.shouts;

import android.content.Context;

import com.appunite.rx.ResponseOrError;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dao.DiscoverShoutsDao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class ShoutsPresenterTest {

    private ShoutsPresenter mShoutsPresenter;

    @Mock
    DiscoverShoutsDao mDiscoverShoutsDao;

    @Mock
    Context mContext;

    private BehaviorSubject<ResponseOrError<ShoutsResponse>> mBehaviorSubject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mBehaviorSubject = BehaviorSubject.create(ResponseOrError.fromData(new ShoutsResponse(0, "", "", ImmutableList.of(new Shout("", "", "", "", new UserLocation(0, 0, "", "", "", "", ""), "", "", 0, 0, "", "", "", null, null, null, 0)))));
        when(mDiscoverShoutsDao.getShoutsObservable(anyString())).thenReturn(mBehaviorSubject);

        mShoutsPresenter = new ShoutsPresenter(Schedulers.immediate(), Schedulers.immediate(), mDiscoverShoutsDao, "", mContext);
    }

    @Test
    public void testShowProgressAndHideAfterSuccess() {
        progressShownAndHidden();
    }

    @Test
    public void testShowProgressAndHideAfterFail() {
        mBehaviorSubject.onNext(ResponseOrError.<ShoutsResponse>fromError(new RuntimeException()));
        progressShownAndHidden();
    }

    private void progressShownAndHidden() {
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mShoutsPresenter.getProgressVisible().subscribe(testSubscriber);

        final TestSubscriber<List<ShoutAdapterItem>> subscriber = new TestSubscriber<>();
        mShoutsPresenter.getSuccessObservable().subscribe(subscriber);

        assert_().that(testSubscriber.getOnNextEvents()).hasSize(2);
        assert_().that(testSubscriber.getOnNextEvents().get(0)).isTrue();
        assert_().that(testSubscriber.getOnNextEvents().get(1)).isFalse();
    }

    @Test
    public void testSuccess() {
        final TestSubscriber<List<ShoutAdapterItem>> subscriber = new TestSubscriber<>();
        mShoutsPresenter.getSuccessObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }

    @Test
    public void testFail() {
        mBehaviorSubject.onNext(ResponseOrError.<ShoutsResponse>fromError(new RuntimeException()));
        final TestSubscriber<Throwable> subscriber = new TestSubscriber<>();
        mShoutsPresenter.getFailObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }
}