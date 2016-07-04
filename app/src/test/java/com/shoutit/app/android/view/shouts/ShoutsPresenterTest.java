package com.shoutit.app.android.view.shouts;

import android.content.Context;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.TestUtils;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.utils.FBAdHalfPresenter;
import com.shoutit.app.android.view.shouts.discover.DiscoverShoutsPresenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class ShoutsPresenterTest {

    private DiscoverShoutsPresenter mShoutsPresenter;

    @Mock
    DiscoverShoutsDao mDiscoverShoutsDao;

    @Mock
    Context mContext;

    @Mock
    UserPreferences userPreferences;

    @Mock
    FBAdHalfPresenter fbAdHalfPresenter;

    private BehaviorSubject<ResponseOrError<ShoutsResponse>> mBehaviorSubject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mBehaviorSubject = BehaviorSubject.create(ResponseOrError.fromData(
                new ShoutsResponse(0, "", "", ImmutableList.of(TestUtils.getShout()), null)));
        when(mDiscoverShoutsDao.getShoutsObservable(anyString())).thenReturn(mBehaviorSubject);

        when(userPreferences.isNormalUser()).thenReturn(true);
        when(userPreferences.getUserOrPage()).thenReturn(TestUtils.getUser());

        when(fbAdHalfPresenter.getAdsObservable(any(Observable.class)))
                .thenReturn(Observable.just(new ArrayList<>()));


        mShoutsPresenter = new DiscoverShoutsPresenter(Schedulers.immediate(), Schedulers.immediate(),
                mDiscoverShoutsDao, "", "", userPreferences, fbAdHalfPresenter, mContext);
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

        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        mShoutsPresenter.getSuccessObservable().subscribe(subscriber);

        assert_().that(testSubscriber.getOnNextEvents()).hasSize(2);
        assert_().that(testSubscriber.getOnNextEvents().get(0)).isTrue();
        assert_().that(testSubscriber.getOnNextEvents().get(1)).isFalse();
    }

    @Test
    public void testSuccess() {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
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