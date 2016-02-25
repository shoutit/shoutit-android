package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.data.UserController;
import com.shoutit.app.android.data.api.ShoutitService;
import com.shoutit.app.android.data.api.image.ShoutitImageType;
import com.shoutit.app.android.data.api.model.Shout;
import com.shoutit.app.android.data.api.model.User;
import com.shoutit.app.android.data.api.model.response.shouts.ShoutStreamResponse;
import com.shoutit.app.android.data.event.users.UserProfileReadyEvent;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.rx.RxMoreObservers;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public abstract class ProfilePresenter {
    private static final int SHOUTS_PAGE_SIZE = 4;

    @Nonnull
    private final Observable<String> avatarObservable;
    @Nonnull
    private final Observable<String> coverUrlObservable;
    @Nonnull
    private final Observable<String> toolbarTitleObservable;
    @Nonnull
    private final Observable<String> toolbarSubtitleObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    @Nonnull
    private final Observable<Throwable> shoutsErrorsResponse;
    @Nonnull
    private final Observable<String> shareObservable;

    @Nonnull
    private final PublishSubject<Boolean> showMoreShoutsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> loadMoreShoutsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> refreshProfileSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Shout> newShoutSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> shareInitSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> listenProfileSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> openChatSubject = PublishSubject.create();

    @Inject
    public ProfilePresenter(@Nonnull final String userName,
                            @Nonnull final ShoutsDao shoutsDao,
                            @Nonnull @ForActivity final Context context,
                            @Nonnull final Resources resources) {

        /** User **/
        final Observable<ResponseOrError<User>> userObservable = getUserObservable()
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        final Observable<User> successUserObservable = userObservable
                .compose(ResponseOrError.<User>onlySuccess());

        /** Header Data **/
        avatarObservable = successUserObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getImage();
                    }
                });

        coverUrlObservable = successUserObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getCover();
                    }
                });

        toolbarTitleObservable = successUserObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return String.format("%1$s %2$s", user.getFirstName(), user.getLastName());
                    }
                });

        toolbarSubtitleObservable = successUserObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return resources.getString(R.string.profile_subtitle, user.getListenersCount());
                    }
                });


        /** Shouts **/
        final Observable<ResponseOrError<ShoutsResponse>> shoutsObservable =
                shoutsDao.getUserShoutObservable(new UserShoutsPointer(SHOUTS_PAGE_SIZE, userName))
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> shoutsSuccessResponse = shoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<Shout>>() {
                    @Override
                    public List<Shout> call(ShoutsResponse response) {
                        return response.getShouts();
                    }
                })
                .filter(Functions1.isNotNull())
                .map(new Func1<List<Shout>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Shout> shouts) {
                        final List<BaseAdapterItem> items = Lists.transform(shouts, new Function<Shout, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(@Nullable Shout shout) {
                                return new ShoutAdapterItem(shout, context, shoutSelectedSubject);
                            }
                        });

                        return ImmutableList.copyOf(items);
                    }
                });

        /** All adapter items **/
        allAdapterItemsObservable = Observable.combineLatest(
                successUserObservable,
                shoutsSuccessResponse.startWith(ImmutableList.<List<BaseAdapterItem>>of()),
                combineAdapterItems());


        /** Errors **/
        shoutsErrorsResponse = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsObservable),
                ResponseOrError.transform(userObservable)))
                .filter(Functions1.isNotNull());

        /** Progress **/
        progressObservable = Observable.merge(userObservable, shoutsErrorsResponse)
                .map(Functions1.returnFalse())
                .startWith(true);

        /** Share **/
        shareObservable = shareInitSubject
                .withLatestFrom(successUserObservable, new Func2<Object, User, String>() {
                    @Override
                    public String call(Object o, User user) {
                        return user.getWebUrl();
                    }
                });
    }

    @NonNull
    protected abstract Func2<User, List<BaseAdapterItem>, List<BaseAdapterItem>> combineAdapterItems();

    @Nonnull
    protected abstract Observable<ResponseOrError<User>> getUserObservable();

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observer<Object> getLoadMoreShoutsSubject() {
        return RxMoreObservers.ignoreCompleted(loadMoreShoutsSubject);
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    @Nonnull
    private Observer<Boolean> getShowAllShoutsObserver() {
        return RxMoreObservers.ignoreCompleted(showMoreShoutsSubject);
    }

    @Nonnull
    public Observable<Throwable> getShoutsErrorsResponse() {
        return shoutsErrorsResponse;
    }

    @Nonnull
    public Observable<String> getAvatarObservable() {
        return avatarObservable;
    }

    @Nonnull
    public Observable<String> getCoverUrlObservable() {
        return coverUrlObservable;
    }

    @Nonnull
    public Observable<String> getToolbarTitleObservable() {
        return toolbarTitleObservable;
    }

    @Nonnull
    public Observable<String> getToolbarSubtitleObservable() {
        return toolbarSubtitleObservable;
    }

    @Nonnull
    public Observer<Object> refreshProfileObserver() {
        return refreshProfileSubject;
    }

    @Nonnull
    public Observer<Shout> getNewShoutSubject() {
        return newShoutSubject;
    }

    @Nonnull
    public Observer<Object> shareProfileInitObserver() {
        return shareInitSubject;
    }

    @Nonnull
    public Observable<String> getShareObservable() {
        return shareObservable;
    }

    public class UserAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final User user;

        public UserAdapterItem(@Nonnull User user) {
            this.user = user;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof UserAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof UserAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }
    }

    public class ProfilePageAdapterItem {

        private final boolean isFirstItem;
        private final boolean isLastItem;
        @Nonnull
        private final Page page;
        @Nonnull
        private final Observer<String> pageSelectedObserver;
        @Nonnull
        private final Observer<String> listenPageObserver;
        @Nonnull
        private final String pageName;

        public ProfilePageAdapterItem(boolean isFirstItem, boolean isLastItem,
                                      @Nonnull Page page,
                                      @Nonnull Observer<String> pageSelectedObserver,
                                      @Nonnull Observer<String> listenPageObserver,
                                      @Nonnull String pageName) {
            this.isFirstItem = isFirstItem;
            this.isLastItem = isLastItem;
            this.page = page;
            this.pageSelectedObserver = pageSelectedObserver;
            this.listenPageObserver = listenPageObserver;
            this.pageName = pageName;
        }

        public void onPageSelected() {
            pageSelectedObserver.onNext(pageName);
        }

        public void onListenPage() {
            if (!page.isListening()) {
                listenPageObserver.onNext(pageName);
            }
        }
    }

    public class ShowMoreShoutsAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Observer<String> showMoreShoutsObserver;
        @Nonnull
        private final String userName;

        public ShowMoreShoutsAdapterItem(@Nonnull Observer<String> showMoreShoutsObserver,
                                         @Nonnull String userName) {
            this.showMoreShoutsObserver = showMoreShoutsObserver;
            this.userName = userName;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ShowMoreShoutsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ShowMoreShoutsAdapterItem;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        public void onShowMoreShouts() {
            showMoreShoutsObserver.onNext(userName);
        }
    }
}
