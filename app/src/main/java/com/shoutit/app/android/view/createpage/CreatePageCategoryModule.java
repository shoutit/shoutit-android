package com.shoutit.app.android.view.createpage;

import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.FileHelper;
import com.shoutit.app.android.view.editprofile.EditProfilePresenter;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class CreatePageCategoryModule {

    @Nullable
    private final EditProfilePresenter.State state;

    public CreatePageCategoryModule(
            @Nullable EditProfilePresenter.State state) {
        this.state = state;
    }

    @Provides
    EditProfilePresenter provideEditProfilePresenter(UserPreferences userPreferences, ApiService apiService,
                                                     FileHelper fileHelper, AmazonHelper amazonHelper,
                                                     @NetworkScheduler Scheduler networkScheduler,
                                                     @UiScheduler Scheduler uiScheduler) {
        return new EditProfilePresenter(userPreferences, apiService, fileHelper, amazonHelper,
                networkScheduler, uiScheduler, state);
    }
}

