package com.shoutit.app.android.view.shout;

import android.content.Context;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.TestUtils;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;
import com.shoutit.app.android.api.model.ConversationDetails;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.dao.UsersIdentityDao;
import com.shoutit.app.android.model.MobilePhoneResponse;
import com.shoutit.app.android.model.RelatedShoutsPointer;
import com.shoutit.app.android.model.UserShoutsPointer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import retrofit2.Response;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class ShoutPresenterTest {

    @Mock
    ShoutsDao shoutsDao;
    @Mock
    Context context;
    @Mock
    UserPreferences userPreferences;
    @Mock
    ShoutsGlobalRefreshPresenter globalRefreshPresenter;
    @Mock
    ShoutsDao.ShoutDao shoutDao;
    @Mock
    UsersIdentityDao userIdentityDao;
    @Mock
    ShoutsDao.RelatedShoutsDao relatedShoutsDao;
    @Mock
    ShoutsDao.UserShoutsDao userShoutsDao;

    private ShoutPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(shoutsDao.getShoutObservable(anyString()))
                .thenReturn(Observable.just(ResponseOrError.fromData(getShout())));
        when(shoutsDao.getUserShoutObservable(any(UserShoutsPointer.class)))
                .thenReturn(Observable.just(ResponseOrError.fromData(new ShoutsResponse(1, "z", "z", Lists.newArrayList(TestUtils.getShout()), null))));
        when(shoutsDao.getRelatedShoutsObservable(any(RelatedShoutsPointer.class)))
                .thenReturn(Observable.just(ResponseOrError.fromData(new ShoutsResponse(1, "z", "z", Lists.newArrayList(TestUtils.getShout()), null))));
        when(userPreferences.getUserObservable())
                .thenReturn(Observable.just(new User("z", null, null, null, null, null, null, null, false, null,
                null, false, false, false, null, 1, null, null, null, 1, null, false, null, null, null, null, null, null, null, null, null)));
        when(userPreferences.isNormalUser())
                .thenReturn(true);
        when(globalRefreshPresenter.getShoutsGlobalRefreshObservable())
                .thenReturn(Observable.just(null));
        when(shoutsDao.getShoutDao(anyString()))
                .thenReturn(shoutDao);
        when(shoutsDao.getShoutDao(anyString()).getRefreshObserver())
                .thenReturn(PublishSubject.create());
        when(shoutsDao.getShoutMobilePhoneObservable(anyString()))
                .thenReturn(Observable.just(ResponseOrError.fromData(new MobilePhoneResponse("123"))));
        when(shoutsDao.getReportShoutObservable(anyString()))
                .thenReturn(Observable.just(Response.success(new Object())));
        when(shoutsDao.getDeleteShoutObservable(anyString()))
                .thenReturn(Observable.just(Response.success(new Object())));
        when(relatedShoutsDao.getRefreshObserver())
                .thenReturn(PublishSubject.create());
        when(shoutsDao.getRelatedShoutsDao(any(RelatedShoutsPointer.class)))
                .thenReturn(relatedShoutsDao);
        when(shoutsDao.getUserShoutsDao(any(UserShoutsPointer.class)))
                .thenReturn(userShoutsDao);
        when(userShoutsDao.getShoutsObservable())
                .thenReturn(Observable.just(ResponseOrError.fromData(new ShoutsResponse(1, "z", "z", Lists.newArrayList(TestUtils.getShout()), null))));

        when(context.getString(anyInt(), anyString()))
                .thenReturn("text");

        when(userPreferences.isNormalUser()).thenReturn(true);
        when(userPreferences.getUser()).thenReturn(TestUtils.getUser());

        presenter = new ShoutPresenter(shoutsDao, "zz", context, Schedulers.immediate(), userPreferences, globalRefreshPresenter);
    }

    @Test
    public void testOnSubscribe_correctItemsReturned() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        final List<BaseAdapterItem> items = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(items.get(0)).isInstanceOf(ShoutAdapterItems.MainShoutAdapterItem.class);
        assert_().that(items.get(1)).isInstanceOf(HeaderAdapterItem.class);
        assert_().that(items.get(2)).isInstanceOf(ShoutAdapterItems.UserShoutAdapterItem.class);
        assert_().that(items.get(3)).isInstanceOf(ShoutAdapterItems.VisitProfileAdapterItem.class);
        assert_().that(items.get(4)).isInstanceOf(HeaderAdapterItem.class);
        assert_().that(items.get(5)).isInstanceOf(ShoutAdapterItems.RelatedContainerAdapterItem.class);
    }

    @Test
    public void testOnSubscribeWithoutUserShouts_correctItemsReturned() throws Exception {
        when(userShoutsDao.getShoutsObservable())
                .thenReturn(Observable.<ResponseOrError<ShoutsResponse>>empty());
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        final List<BaseAdapterItem> items = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(items.get(0)).isInstanceOf(ShoutAdapterItems.MainShoutAdapterItem.class);
        assert_().that(items.get(1)).isInstanceOf(ShoutAdapterItems.VisitProfileAdapterItem.class);
        assert_().that(items.get(2)).isInstanceOf(HeaderAdapterItem.class);
        assert_().that(items.get(3)).isInstanceOf(ShoutAdapterItems.RelatedContainerAdapterItem.class);
    }

    @Test
    public void testOnSubscribeWithoutRelatedShouts_correctItemsReturned() throws Exception {
        when(shoutsDao.getRelatedShoutsObservable(any(RelatedShoutsPointer.class)))
                .thenReturn(Observable.<ResponseOrError<ShoutsResponse>>empty());
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        final List<BaseAdapterItem> items = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(items.get(0)).isInstanceOf(ShoutAdapterItems.MainShoutAdapterItem.class);
        assert_().that(items.get(1)).isInstanceOf(HeaderAdapterItem.class);
        assert_().that(items.get(2)).isInstanceOf(ShoutAdapterItems.UserShoutAdapterItem.class);
        assert_().that(items.get(3)).isInstanceOf(ShoutAdapterItems.VisitProfileAdapterItem.class);
        assert_().that(items.get(4)).isInstanceOf(HeaderAdapterItem.class);
    }

    @Test
    public void testOnSubscribeWithoutRelatedShoutsAndUserShouts_correctItemsReturned() throws Exception {
        when(shoutsDao.getRelatedShoutsObservable(any(RelatedShoutsPointer.class)))
                .thenReturn(Observable.<ResponseOrError<ShoutsResponse>>empty());
        when(shoutsDao.getUserShoutObservable(any(UserShoutsPointer.class)))
                .thenReturn(Observable.<ResponseOrError<ShoutsResponse>>empty());
        final TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();
        presenter.getAllAdapterItemsObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        final List<BaseAdapterItem> items = Iterables.getLast(subscriber.getOnNextEvents());
        assert_().that(items.get(0)).isInstanceOf(ShoutAdapterItems.MainShoutAdapterItem.class);
    }

    @Test
    public void testOnSuccess_correctProgressSequence() throws Exception {
        final TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getProgressObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        assert_().that(subscriber.getOnNextEvents().get(0)).isEqualTo(true);
        assert_().that(Iterables.getLast(subscriber.getOnNextEvents())).isEqualTo(false);
    }

    @Test
    public void testOnFail_correctProgressSequence() throws Exception {
        when(shoutsDao.getShoutObservable(anyString()))
                .thenReturn(Observable.just(ResponseOrError.<Shout>fromError(new Throwable())));
        final TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        presenter.getProgressObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        assert_().that(subscriber.getOnNextEvents().get(0)).isEqualTo(true);
        assert_().that(Iterables.getLast(subscriber.getOnNextEvents())).isEqualTo(false);
    }

    @Test
    public void testOnFail_errorDisplayed() throws Exception {
        when(userShoutsDao.getShoutsObservable())
                .thenReturn(Observable.just(ResponseOrError.<ShoutsResponse>fromError(new Throwable("error"))));
        final TestSubscriber<Throwable> subscriber = new TestSubscriber<>();
        presenter.getErrorObservable().subscribe(subscriber);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(2);
    }

    private Shout getShout() {
        return new Shout("id", null, null, null, null, null, null, 1L, 2, null, null, null,
                getUser(), null, null, 1, null, null, 0, ImmutableList.<ConversationDetails>of(), true, null, null, promotion);
    }

    private User getUser() {
        return new User("z", null, null, null, null, null, null, null, false, null,
                null, false, false, false, null, 1, null, null, null, 1, null, false, null, null, null, null, null, null, null, null, null);
    }


}
