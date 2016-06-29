package com.shoutit.app.android.view.invitefriends.suggestionsusers;

import android.content.res.Resources;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class UserSuggestionPresenter extends BaseProfileListPresenter {

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;

    public UserSuggestionPresenter(@Nonnull ProfilesDao dao,
                                   @Nonnull @UiScheduler Scheduler uiScheduler,
                                   @Nonnull @ForActivity Resources resources,
                                   @Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                   @Nonnull UserPreferences userPreferences) {
        super(listeningHalfPresenter, uiScheduler, resources.getString(R.string.post_no_suggested_users), userPreferences);

        final Observable<ProfilesDao.FriendsSuggestionPointer> pointerObservable = userPreferences
                .getPageOrUserObservable()
                .filter(Functions1.isNotNull())
                .map(baseProfile -> new ProfilesDao.FriendsSuggestionPointer(baseProfile.getLocation(), baseProfile.getUsername()));

        daoObservable = pointerObservable
                .filter(Functions1.isNotNull())
                .map(dao::getUsersSuggestionDao)
                .compose(ObservableExtensions.behaviorRefCount());

        init();
    }

    @Override
    @Nonnull
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }
}
