package com.shoutit.app.android.view.profileslist;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public abstract class BaseProfileListPresenter {

    @Nonnull
    private final ListeningHalfPresenter listeningHalfPresenter;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nullable
    private final String placeholderText;

    private Observable<BaseProfileListDao> daoObservable;
    private Observable<Object> refreshDataObservable;
    private Observable<BaseProfileListDao> listeningObservable;
    private Observable<Throwable> errorObservable;
    private Observable<Boolean> progressObservable;
    private Observable<Object> loadMoreObservable;

    @Nonnull
    private final PublishSubject<Object> actionOnlyForLoggedInUsers = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> refreshDataSubject = PublishSubject.create();
    @Nonnull
    private Observable<ProfilesListResponse> successRequestObservable;

    protected boolean isNormalUser;

    public BaseProfileListPresenter(@Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                    @Nonnull @UiScheduler Scheduler uiScheduler,
                                    @Nullable String placeholderText,
                                    @Nonnull UserPreferences userPreferences) {
        this.listeningHalfPresenter = listeningHalfPresenter;
        this.uiScheduler = uiScheduler;
        this.placeholderText = placeholderText;

        isNormalUser = userPreferences.isNormalUser();
    }

    // Must be called at the end of child constructor
    protected void init() {
        daoObservable = getDaoObservable()
                .compose(ObservableExtensions.behaviorRefCount());

        final Observable<ResponseOrError<ProfilesListResponse>> requestObservable =
                daoObservable.flatMap(dao -> dao.getProfilesObservable()
                        .observeOn(uiScheduler))
                        .compose(ObservableExtensions.behaviorRefCount());

        successRequestObservable = requestObservable
                .compose(ResponseOrError.onlySuccess());

        listeningObservable = listeningHalfPresenter
                .listeningObservable(successRequestObservable)
                .switchMap(updatedProfile -> daoObservable
                        .map((Func1<BaseProfileListDao, BaseProfileListDao>) dao -> {
                            dao.updatedProfileLocallyObserver().onNext(updatedProfile);
                            return null;
                        }));

        errorObservable = requestObservable
                .compose(ResponseOrError.onlyError())
                .mergeWith(listeningHalfPresenter.getErrorSubject());

        progressObservable = requestObservable.map(Functions1.returnFalse())
                .startWith(true);

        loadMoreObservable = loadMoreSubject
                .withLatestFrom(daoObservable, (ignore, dao) -> {
                    dao.getLoadMoreObserver().onNext(null);
                    return null;
                });

        refreshDataObservable = refreshDataSubject
                .withLatestFrom(daoObservable, (o, dao) -> {
                    dao.getRefreshSubject().onNext(null);
                    return null;
                });
    }

    @Nonnull
    protected Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return successRequestObservable
                .map((Func1<ProfilesListResponse, List<BaseAdapterItem>>) pagesResponse -> {
                    final List<BaseProfile> pages = pagesResponse.getResults();

                    if (pages.isEmpty()) {
                        return ImmutableList.of(new NoDataTextAdapterItem(placeholderText));
                    } else {
                        return ImmutableList.copyOf(
                                Lists.transform(pages, new Function<BaseProfile, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public BaseAdapterItem apply(BaseProfile profile) {
                                        return new ProfileListAdapterItem(profile, openProfileSubject,
                                                listeningHalfPresenter.getListenProfileSubject(),
                                                actionOnlyForLoggedInUsers, isNormalUser, profile.isOwner());
                                    }
                                }));
                    }
                });
    }

    protected abstract Observable<BaseProfileListDao> getDaoObservable();

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    public Observable<String> getProfileToOpenObservable() {
        return openProfileSubject;
    }

    public void refreshData() {
        refreshDataSubject.onNext(null);
    }

    @Nonnull
    protected Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(loadMoreSubject);
    }

    @Nonnull
    public Observable<String> getListenSuccessObservable() {
        return listeningHalfPresenter.getListenSuccess();
    }

    @Nonnull
    public Observable<String> getUnListenSuccessObservable() {
        return listeningHalfPresenter.getUnListenSuccess();
    }

    @Nonnull
    public Observable<Object> getLoadMoreObservable() {
        return loadMoreObservable;
    }

    @Nonnull
    public Observable<Object> getRefreshDataObservable() {
        return refreshDataObservable;
    }

    @Nonnull
    public Observable<BaseProfileListDao> getListeningObservable() {
        return listeningObservable;
    }

    @Nonnull
    public Observer<Object> getRefreshDataObserver() {
        return refreshDataSubject;
    }

    @Nonnull
    public Observable<Object> getActionOnlyForLoggedInUsers() {
        return actionOnlyForLoggedInUsers;
    }
}




