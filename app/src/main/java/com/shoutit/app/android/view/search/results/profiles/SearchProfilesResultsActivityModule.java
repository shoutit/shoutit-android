package com.shoutit.app.android.view.search.results.profiles;

import android.content.res.Resources;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

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
    @ActivityScope
    BaseProfileListPresenter provideProfilesWithoutPagesListPresenter(ProfilesDao dao,
                                                                      @UiScheduler Scheduler uiScheduler,
                                                                      ListeningHalfPresenter listeningHalfPresenter,
                                                                      UserPreferences userPreferences,
                                                                      @ForActivity Resources resources) {
        return new SearchProfilesResultsPresenter(dao, searchQuery, uiScheduler,
                userPreferences, resources, listeningHalfPresenter);
    }
}
