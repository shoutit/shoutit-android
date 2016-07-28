package com.shoutit.app.android.view.search.results.profiles;

import android.content.res.Resources;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class SearchProfilesResultsPresenter extends BaseProfileListPresenter {

    private final ProfilesDao dao;
    @Nonnull
    private final String searchQuery;

    public SearchProfilesResultsPresenter(ProfilesDao dao, @Nonnull String searchQuery,
                                          @UiScheduler final Scheduler uiScheduler,
                                          UserPreferences userPreferences,
                                          Resources resources,
                                          ListeningHalfPresenter listeningHalfPresenter) {
        super(listeningHalfPresenter, uiScheduler, resources.getString(R.string.search_results_no_results), userPreferences);
        this.dao = dao;
        this.searchQuery = searchQuery;

        init();
    }

    @Override
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return Observable.just(dao.getSearchProfilesDao(searchQuery));
    }


}
