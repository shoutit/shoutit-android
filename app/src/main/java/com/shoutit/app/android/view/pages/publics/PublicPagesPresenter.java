package com.shoutit.app.android.view.pages.publics;

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
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.PublicPagesDaos;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.ProfileListAdapterItem;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class PublicPagesPresenter extends ProfilesListPresenter {

    private final Observable<Object> loadMoreObservable;
    private final Observable<PublicPagesDaos.PublicPagesDao> listeningObservable;
    private final Observable<List<BaseAdapterItem>> pagesObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<PublicPagesDaos.PublicPagesDao> daoObservable;
    private final Observable<Object> refreshDataObservable;

    protected final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
    protected final PublishSubject<Object> actionOnlyForLoggedInUsers = PublishSubject.create();
    protected final PublishSubject<String> openProfileSubject = PublishSubject.create();
    protected final PublishSubject<Object> refreshDataSubject = PublishSubject.create();

    @Inject
    public PublicPagesPresenter(@Nonnull PublicPagesDaos pagesDaos,
                                @Nonnull @UiScheduler Scheduler uiScheduler,
                                @Nonnull @ForActivity Resources resources,
                                @Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                @Nonnull UserPreferences userPreferences) {
        super(listeningHalfPresenter);

        daoObservable = userPreferences
                .getUserObservable()
                .filter(Functions1.isNotNull())
                .map(User::getLocation)
                .filter(Functions1.isNotNull())
                .map(UserLocation::getCountry)
                .map(pagesDaos::getDao)
                .compose(ObservableExtensions.behaviorRefCount());

        final Observable<ResponseOrError<ProfilesListResponse>> requestObservable =
                daoObservable.flatMap(pagesDao -> pagesDao.getPagesObservable()
                        .observeOn(uiScheduler))
                        .compose(ObservableExtensions.behaviorRefCount());

        final Observable<ProfilesListResponse> successRequestObservable = requestObservable
                .compose(ResponseOrError.onlySuccess());

        pagesObservable = requestObservable
                .compose(ResponseOrError.onlySuccess())
                .map((Func1<ProfilesListResponse, List<BaseAdapterItem>>) pagesResponse -> {
                    final List<BaseProfile> pages = pagesResponse.getResults();

                    if (pages.isEmpty()) {
                        return ImmutableList.of(new NoDataTextAdapterItem(resources.getString(R.string.pages_empty)));
                    } else {
                        return ImmutableList.copyOf(
                                Lists.transform(pages, new Function<BaseProfile, BaseAdapterItem>() {
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
                        .map((Func1<PublicPagesDaos.PublicPagesDao, PublicPagesDaos.PublicPagesDao>) dao -> {
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
                    dao.getLoadMoreSubject().onNext(null);
                    return null;
                });

        refreshDataObservable = refreshDataSubject
                .withLatestFrom(daoObservable, (o, dao) -> {
                    dao.getRefreshSubject().onNext(null);
                    return null;
                });
    }

    public Observable<PublicPagesDaos.PublicPagesDao> getListeningObservable() {
        return listeningObservable;
    }

    public Observable<Object> getRefreshDataObservable() {
        return refreshDataObservable;
    }

    public Observable<Object> getLoadMoreObservable() {
        return loadMoreObservable;
    }

    @Override
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Override
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Override
    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return pagesObservable;
    }

    @Override
    public Observable<String> getProfileToOpenObservable() {
        return openProfileSubject;
    }

    @Override
    public void refreshData() {
        refreshDataSubject.onNext(null);
    }

    @Override
    public Observer<Object> getLoadMoreObserver() {
        return loadMoreSubject;
    }
}
