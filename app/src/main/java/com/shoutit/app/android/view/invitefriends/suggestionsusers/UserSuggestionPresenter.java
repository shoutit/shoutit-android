package com.shoutit.app.android.view.invitefriends.suggestionsusers;

import android.content.res.Resources;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.constants.RequestsConstants;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.ProfileListAdapterItem;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class UserSuggestionPresenter extends ProfilesListPresenter {

    private final Observable<Object> loadMoreObservable;
    private final Observable<ProfilesDao.UsersSuggestionDao> listeningObservable;
    private final Observable<List<BaseAdapterItem>> usersObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<ProfilesDao.UsersSuggestionDao> daoObservable;
    private final Observable<Object> refreshDataObservable;

    protected final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
    protected final PublishSubject<Object> actionOnlyForLoggedInUsers = PublishSubject.create();
    protected final PublishSubject<String> openProfileSubject = PublishSubject.create();
    protected final PublishSubject<Object> refreshDataSubject = PublishSubject.create();

    public UserSuggestionPresenter(@Nonnull ProfilesDao dao,
                                   @Nonnull @UiScheduler Scheduler uiScheduler,
                                   @Nonnull @ForActivity Resources resources,
                                   @Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                   @Nonnull UserPreferences userPreferences) {
        super(listeningHalfPresenter);

        final Observable<ProfilesDao.FriendsSuggestionPointer> pointerObservable = Observable
                .zip(
                        userPreferences.getLocationObservable().filter(Functions1.isNotNull()),
                        userPreferences.getUserObservable().filter(Functions1.isNotNull()),
                        (userLocation, user) -> new ProfilesDao.FriendsSuggestionPointer(RequestsConstants.PAGE_SIZE, userLocation, user.getUsername()));

        daoObservable = pointerObservable
                .filter(Functions1.isNotNull())
                .map(dao::getUsersSuggestionDao)
                .compose(ObservableExtensions.behaviorRefCount());

        final Observable<ResponseOrError<ProfilesListResponse>> requestObservable =
                daoObservable
                        .flatMap(ProfilesDao.BaseProfileListDao::getProfilesObservable)
                        .observeOn(uiScheduler)
                        .compose(ObservableExtensions.behaviorRefCount());

        final Observable<ProfilesListResponse> successRequestObservable = requestObservable
                .compose(ResponseOrError.onlySuccess());

        usersObservable = requestObservable
                .compose(ResponseOrError.onlySuccess())
                .map((Func1<ProfilesListResponse, List<BaseAdapterItem>>) userResponse -> {
                    final List<BaseProfile> users = userResponse.getResults();

                    if (users.isEmpty()) {
                        return ImmutableList.of(new NoDataTextAdapterItem(resources.getString(R.string.no_suggested_users)));
                    } else {
                        return ImmutableList.copyOf(
                                Lists.transform(users, new Function<BaseProfile, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public BaseAdapterItem apply(BaseProfile page) {
                                        return new ProfileListAdapterItem(page, openProfileSubject,
                                                listeningHalfPresenter.getListenProfileSubject(),
                                                actionOnlyForLoggedInUsers, true, page.isOwner());
                                    }
                                }));
                    }
                });

        listeningObservable = listeningHalfPresenter
                .listeningObservable(successRequestObservable)
                .switchMap(updatedProfile -> daoObservable
                        .map(daos -> {
                            daos.updatedProfileLocallyObserver().onNext(updatedProfile);
                            return null;
                        }));

        errorObservable = requestObservable
                .compose(ResponseOrError.onlyError())
                .mergeWith(listeningHalfPresenter.getErrorSubject());

        progressObservable = requestObservable.map(Functions1.returnFalse())
                .startWith(true);

        loadMoreObservable = loadMoreSubject
                .withLatestFrom(daoObservable, (ignore, daos) -> {
                    daos.getLoadMoreShoutsObserver().onNext(null);
                    return null;
                });

        refreshDataObservable = refreshDataSubject
                .withLatestFrom(daoObservable, (o, daos) -> {
                    daos.getRefreshSubject().onNext(null);
                    return null;
                });
    }

    public Observable<ProfilesDao.UsersSuggestionDao> getListeningObservable() {
        return listeningObservable;
    }

    @Override
    protected Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Override
    protected Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Override
    protected Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return usersObservable;
    }

    @Override
    protected Observable<String> getProfileToOpenObservable() {
        return openProfileSubject;
    }

    @Override
    protected void refreshData() {
        refreshDataSubject.onNext(null);
    }

    public Observable<Object> getRefreshDataObservable() {
        return refreshDataObservable;
    }

    @Override
    protected Observer<Object> getLoadMoreObserver() {
        return loadMoreSubject;
    }

    public Observable<Object> getLoadMoreObservable() {
        return loadMoreObservable;
    }
}
