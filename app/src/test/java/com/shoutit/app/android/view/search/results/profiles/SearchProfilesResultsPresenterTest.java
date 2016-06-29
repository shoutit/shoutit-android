package com.shoutit.app.android.view.search.results.profiles;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.SearchProfileResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ProfilesDao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

public class SearchProfilesResultsPresenterTest {

    @Mock
    ApiService apiService;
    @Mock
    UserPreferences userPreferences;

    private ProfilesDao profilesDao;
    private SearchProfilesResultsPresenter presenter;
    private TestScheduler testScheduler = new TestScheduler();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(userPreferences.isNormalUser())
                .thenReturn(true);
        when(apiService.unlistenProfile(anyString()))
                .thenReturn(Observable.just(ResponseBody.create(null, "z")));
        when(apiService.listenProfile(anyString()))
                .thenReturn(Observable.just(ResponseBody.create(null, "z")));
        when(apiService.searchProfiles(anyString(), anyInt(), anyInt()))
                .thenReturn(Observable.just(getSearchResponse()));

        profilesDao = new ProfilesDao(apiService, testScheduler, userPreferences);
        presenter = new SearchProfilesResultsPresenter(profilesDao, "zzz", apiService, testScheduler, testScheduler, userPreferences);

    }

    @Test
    public void testOnSubscribe_correctItemsReturned() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();

        presenter.getAdapterItemsObservable()
                .subscribe(subscriber);
        testScheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        assert_().that(getLastItem(subscriber)).isInstanceOf(SearchProfilesResultsPresenter.ProfileAdapterItem.class);
    }

    @Test
    public void testOnProfileListened_profileUpdated() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();

        presenter.getAdapterItemsObservable()
                .subscribe(subscriber);
        testScheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final SearchProfilesResultsPresenter.ProfileAdapterItem item = getLastItem(subscriber);
        final User profileBeforeUpdate = item.getProfile();

        item.onProfileListened();
        testScheduler.triggerActions();

        subscriber.assertValueCount(2);
        final SearchProfilesResultsPresenter.ProfileAdapterItem updatedResponse = getLastItem(subscriber);
        final User updatedProfile = updatedResponse.getProfile();
        assert_().that(updatedProfile.isListening()).isEqualTo(!profileBeforeUpdate.isListening());
        assert_().that(updatedProfile.getListenersCount()).isEqualTo(profileBeforeUpdate.getListenersCount() + 1);
    }

    @Test
    public void testOnProfileListenedTwice_profileUpdated() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();

        presenter.getAdapterItemsObservable()
                .subscribe(subscriber);
        testScheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final SearchProfilesResultsPresenter.ProfileAdapterItem item = getLastItem(subscriber);
        final User profileBeforeUpdate = item.getProfile();

        item.onProfileListened();
        testScheduler.triggerActions();
        getLastItem(subscriber).onProfileListened();
        testScheduler.triggerActions();

        subscriber.assertValueCount(3);
        final SearchProfilesResultsPresenter.ProfileAdapterItem updatedResponse = getLastItem(subscriber);
        final User updatedProfile = updatedResponse.getProfile();
        assert_().that(updatedProfile.isListening()).isEqualTo(profileBeforeUpdate.isListening());
        assert_().that(updatedProfile.getListenersCount()).isEqualTo(profileBeforeUpdate.getListenersCount());
    }

    @Test
    public void testWhenRefreshedData_newDataArrived() throws Exception {
        TestSubscriber<List<BaseAdapterItem>> subscriber = new TestSubscriber<>();

        presenter.getAdapterItemsObservable()
                .subscribe(subscriber);
        testScheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);

        presenter.refreshData();
        testScheduler.triggerActions();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(2);
    }

    private SearchProfilesResultsPresenter.ProfileAdapterItem getLastItem(TestSubscriber<List<BaseAdapterItem>> subscriber) {
        return (SearchProfilesResultsPresenter.ProfileAdapterItem) Iterables.getLast(subscriber.getOnNextEvents()).get(0);
    }

    private SearchProfileResponse getSearchResponse() {
        return new SearchProfileResponse(1, "l", "z", Lists.newArrayList(
                new User("1", null, null, null, "username", null, null, null, false, null, null,
                        false, false, false, null, 1, null, 1, null, false, null, null, null, null, null, null, null, null, null, null)));
    }
}
