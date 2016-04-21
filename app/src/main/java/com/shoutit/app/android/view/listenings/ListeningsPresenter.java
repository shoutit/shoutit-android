package com.shoutit.app.android.view.listenings;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.Tag;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ListeningsPresenter {

    private final Observable<List<BaseAdapterItem>> adapterItemsObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;

    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();

    @Nonnull
    private final ListeningsDao listeningsDao;

    @Inject
    public ListeningsPresenter(@UiScheduler final Scheduler uiScheduler,
                               @Nonnull ListeningsDao listeningsDao) {
        this.listeningsDao = listeningsDao;

        final Observable<ResponseOrError<ListeningResponse>> profilesAndTagsObservable = listeningsDao
                .getLsteningObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ListeningResponse>>behaviorRefCount());

        final Observable<ListeningResponse> successProfilesAndTagsObservable = profilesAndTagsObservable
                .compose(ResponseOrError.<ListeningResponse>onlySuccess());

        adapterItemsObservable = successProfilesAndTagsObservable
                .map(new Func1<ListeningResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ListeningResponse listeningResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        if (listeningResponse.getUsers() != null) {
                            for (User user : listeningResponse.getUsers()) {
                                builder.add(new ProfileAdapterItem(user, openProfileSubject));
                            }
                        }

                        if (listeningResponse.getPages() != null) {
                            for (Page page : listeningResponse.getPages()) {
                                builder.add(new ProfileAdapterItem(page, openProfileSubject));
                            }
                        }

                        if (listeningResponse.getTags() != null) {
                            for (Tag tag : listeningResponse.getTags()) {
                                builder.add(new TagAdapterItem(tag, openProfileSubject));
                            }
                        }

                        final ImmutableList<BaseAdapterItem> items = builder.build();
                        if (items.size() == 0) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataAdapterItem());
                        } else {
                            return items;
                        }
                    }
                });

        progressObservable = successProfilesAndTagsObservable.map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = profilesAndTagsObservable.compose(ResponseOrError.<ListeningResponse>onlyError());
    }

    @Nonnull
    public Observable<String> getListenSuccessObservable() {
        return listenSuccess;
    }

    @Nonnull
    public Observable<String> getUnListenSuccessObservable() {
        return unListenSuccess;
    }

    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(listeningsDao.getLoadMoreObserver());
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return adapterItemsObservable;
    }

    public Observable<String> getProfileToOpenObservable() {
        return openProfileSubject;
    }

    public void refreshData() {
        listeningsDao.getRefreshSubject().onNext(null);
    }

    class ProfileAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final BaseProfile baseProfile;
        @Nonnull
        private final Observer<String> openProfileObserver;

        public ProfileAdapterItem(@Nonnull BaseProfile baseProfile,
                                  @Nonnull Observer<String> openProfileObserver) {
            this.baseProfile = baseProfile;
            this.openProfileObserver = openProfileObserver;
        }

        public void onProfileOpen() {
            openProfileObserver.onNext(baseProfile.getUsername());
        }

        @Nonnull
        public BaseProfile getProfile() {
            return baseProfile;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItem &&
                    baseProfile.getUsername().equals(((ProfileAdapterItem) item).baseProfile.getUsername());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItem &&
                    baseProfile.equals(((ProfileAdapterItem) item).baseProfile);
        }
    }

    class TagAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Tag tag;
        @Nonnull
        private final Observer<String> openProfileObserver;

        public TagAdapterItem(@Nonnull Tag tag,
                              @Nonnull Observer<String> openProfileObserver) {
            this.tag = tag;
            this.openProfileObserver = openProfileObserver;
        }

        public void onProfileOpen() {
            openProfileObserver.onNext(tag.getName());
        }

        @Nonnull
        public Tag getTag() {
            return tag;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof TagAdapterItem &&
                    tag.getName().equals(((TagAdapterItem) item).tag.getName());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof TagAdapterItem &&
                    tag.equals(((TagAdapterItem) item).tag);
        }
    }
}
