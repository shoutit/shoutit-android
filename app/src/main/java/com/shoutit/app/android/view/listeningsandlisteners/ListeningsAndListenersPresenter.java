package com.shoutit.app.android.view.listeningsandlisteners;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.User;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public abstract class ListeningsAndListenersPresenter {

    private Observable<List<BaseAdapterItem>> adapterItemsObservable;
    private Observable<Boolean> progressObservable;
    private Observable<Throwable> errorObservable;

    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    private final Scheduler uiScheduler;

    public ListeningsAndListenersPresenter(@UiScheduler final Scheduler uiScheduler) {
        this.uiScheduler = uiScheduler;
    }

    public void initPresenter() {
        final Observable<ResponseOrError<ListeningResponse>> profilesAndTagsObservable = getRequestObservable()
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
                            for (TagDetail tag : listeningResponse.getTags()) {
                                builder.add(new ProfileAdapterItem(tag, openProfileSubject));
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

    public abstract void refreshData();

    public abstract Observable<ResponseOrError<ListeningResponse>> getRequestObservable();

    public abstract Observer<Object> getLoadMoreObserver();

    public class ProfileAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final ProfileType profile;
        @Nonnull
        private final Observer<String> openProfileObserver;

        public ProfileAdapterItem(@Nonnull ProfileType profile,
                                  @Nonnull Observer<String> openProfileObserver) {
            this.profile = profile;
            this.openProfileObserver = openProfileObserver;
        }

        public void openProfile() {
            openProfileObserver.onNext(profile.getUsername());
        }

        @Nonnull
        public ProfileType getProfile() {
            return profile;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItem &&
                    profile.getUsername().equals(((ProfileAdapterItem) item).profile.getUsername());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItem &&
                    profile.equals(((ProfileAdapterItem) item).profile);
        }
    }
}
