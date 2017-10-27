package com.shoutit.app.android.dagger;

import com.appunite.rx.dagger.NetworkScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.BusinessVerificationDaos;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.ConversationMediaDaos;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.dao.PagesDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.PromoteLabelsDao;
import com.shoutit.app.android.dao.PromoteOptionsDao;
import com.shoutit.app.android.dao.PublicPagesDaos;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.dao.SuggestionsDao;
import com.shoutit.app.android.dao.TagListDaos;
import com.shoutit.app.android.dao.TagsDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class DaoModule {

    @Provides
    @Singleton
    SuggestionsDao provideSuggestionsDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new SuggestionsDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    CategoriesDao categoriesDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new CategoriesDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    ListenersDaos listenersDaos(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new ListenersDaos(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    ListeningsDao listeningsDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new ListeningsDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    PublicPagesDaos publicPagesDaos(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new PublicPagesDaos(apiService, networkScheduler);
    }

    @Singleton
    @Provides
    ConversationMediaDaos provideConversationMediaDaos(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new ConversationMediaDaos(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    SortTypesDao sortTypesDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new SortTypesDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    BusinessVerificationDaos businessVerificationDaos(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new BusinessVerificationDaos(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    PromoteLabelsDao promoteDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new PromoteLabelsDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    PromoteOptionsDao promoteOptionsDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new PromoteOptionsDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    PagesDao providePagesDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new PagesDao(apiService, networkScheduler);
    }

    @Provides
    @Singleton
    TagListDaos provideTagsListDao(ApiService apiService, @NetworkScheduler Scheduler networkScheduler) {
        return new TagListDaos(apiService, networkScheduler);
    }

    @Singleton
    @Provides
    public DiscoverShoutsDao provideDiscoverShoutsDao(@NetworkScheduler Scheduler networkScheduler,
                                                      ApiService apiService) {
        return new DiscoverShoutsDao(networkScheduler, apiService);
    }

    @Singleton
    @Provides
    public ProfilesDao provideProfilesDao(ApiService apiService,
                                          @NetworkScheduler Scheduler networkScheduler,
                                          UserPreferences userPreferences) {
        return new ProfilesDao(apiService, networkScheduler, userPreferences);
    }

    @Singleton
    @Provides
    public TagsDao provideTagsDao(ApiService apiService,
                                  @NetworkScheduler Scheduler networkScheduler) {
        return new TagsDao(apiService, networkScheduler);
    }

    @Singleton
    @Provides
    public ShoutsDao provideShoutsDao(ApiService apiService,
                                      @NetworkScheduler Scheduler networkScheduler,
                                      UserPreferences userPreferences) {
        return new ShoutsDao(apiService, networkScheduler, userPreferences);
    }

    @Singleton
    @Provides
    public DiscoversDao provideDiscoversDao(ApiService apiService,
                                            @NetworkScheduler Scheduler networkScheduler) {
        return new DiscoversDao(apiService, networkScheduler);
    }
}
