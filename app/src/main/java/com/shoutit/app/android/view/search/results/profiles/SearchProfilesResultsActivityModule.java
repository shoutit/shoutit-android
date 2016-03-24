package com.shoutit.app.android.view.search.results.profiles;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.ProfilesDao;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class SearchProfilesResultsActivityModule {

    private final String searchQuery;

    public SearchProfilesResultsActivityModule(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Provides
    SearchProfilesResultsPresenter provideSearchProfilesResultsPresenter(ProfilesDao dao, ApiService apiService,
                                                                         @UiScheduler Scheduler uiScheduler,
                                                                         @NetworkScheduler Scheduler networkScheduler,
                                                                         UserPreferences userPreferences) {
        return new SearchProfilesResultsPresenter(dao, searchQuery, apiService, uiScheduler, networkScheduler, userPreferences);
    }
}
