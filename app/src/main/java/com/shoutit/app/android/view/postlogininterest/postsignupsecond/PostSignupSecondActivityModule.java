package com.shoutit.app.android.view.postlogininterest.postsignupsecond;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.SuggestionsDao;



import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class PostSignupSecondActivityModule {

    @Provides
    @ActivityScope
    PostSignupSecondPresenter providesPostSignupSecondPresenter(SuggestionsDao suggestionsDao, ApiService apiService,
                                                                UserPreferences userPreferences, @NetworkScheduler Scheduler networkScheduler,
                                                                @UiScheduler Scheduler uiScheduler) {
        return new PostSignupSecondPresenter(suggestionsDao, apiService, userPreferences, networkScheduler, uiScheduler);
    }
}
