package com.shoutit.app.android.view.pages.my;

import android.support.annotation.NonNull;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Scheduler;

public class SelectListenersWithoutPagesPresenter extends BaseProfileListPresenter {

    private class ListenersWithoutPagesDao extends BaseProfileListDao {

        private final BaseProfileListDao baseDao;

        public ListenersWithoutPagesDao(@Nonnull String userName, @Nonnull @NetworkScheduler Scheduler networkScheduler, BaseProfileListDao baseDao) {
            super(userName, networkScheduler);
            this.baseDao = baseDao;
        }

        @Override
        public Observable<ProfilesListResponse> getRequest(int pageNumber) {
            return baseDao.getRequest(pageNumber)
                    .map(profilesListResponse -> {
                        final List<BaseProfile> newResults = ImmutableList.copyOf(Iterables.filter(profilesListResponse.getResults(), input -> {
                            assert input != null;
                            return input.isUser();
                        }));
                        return new ProfilesListResponse(profilesListResponse.getCount(), profilesListResponse.getNext(), profilesListResponse.getPrevious(), newResults);
                    });
        }
    }

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;

    public SelectListenersWithoutPagesPresenter(@Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                                @Nonnull @UiScheduler Scheduler uiScheduler,
                                                @Nonnull @NetworkScheduler Scheduler networkScheduler,
                                                @Nullable String placeholderText,
                                                @Nonnull UserPreferences userPreferences,
                                                @Nonnull ListenersDaos listenersDaos) {
        super(listeningHalfPresenter, uiScheduler, placeholderText, userPreferences);

        final String username = userPreferences.getUserOrPage().getUsername();

        daoObservable = Observable.just(new ListenersWithoutPagesDao(username, networkScheduler, listenersDaos.getDao(username)));

        init();
    }

    @NonNull
    @Override
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }
}