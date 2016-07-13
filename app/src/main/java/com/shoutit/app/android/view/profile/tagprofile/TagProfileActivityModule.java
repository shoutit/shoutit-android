package com.shoutit.app.android.view.profile.tagprofile;

import android.content.Context;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.TagsDao;
import com.shoutit.app.android.utils.BookmarkHelper;
import com.shoutit.app.android.view.profile.user.ProfilePresenter;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class TagProfileActivityModule {

    @Nonnull
    private final String slugName;

    public TagProfileActivityModule(@Nonnull String slugName) {
        this.slugName = slugName;
    }

    @Provides
    @ActivityScope
    public ProfilePresenter provideTagProfilePresenter(TagsDao tagsDao, ShoutsDao shoutsDao, @ForActivity Context context,
                                                       @UiScheduler Scheduler uiScheduler,
                                                       @NetworkScheduler Scheduler networkScheduler, ApiService apiService,
                                                       UserPreferences userPreferences,
                                                       BookmarksDao bookmarksDao,
                                                       BookmarkHelper bookmarkHelper) {
        return new TagProfilePresenter(tagsDao, shoutsDao, slugName, uiScheduler, networkScheduler,
                apiService, context, userPreferences, bookmarksDao, bookmarkHelper);
    }
}
