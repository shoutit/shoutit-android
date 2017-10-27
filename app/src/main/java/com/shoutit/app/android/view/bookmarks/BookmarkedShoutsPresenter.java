package com.shoutit.app.android.view.bookmarks;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.utils.BookmarkHelper;
import com.shoutit.app.android.utils.PromotionHelper;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.shoutit.app.android.view.shouts_list_common.ShoutListPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class BookmarkedShoutsPresenter implements ShoutListPresenter {

    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();

    @Nonnull
    private final Observable<List<BaseAdapterItem>> adapterItemsObservable;
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @NonNull
    private final BookmarkHelper mHelper;

    @Inject
    public BookmarkedShoutsPresenter(@Nonnull @UiScheduler Scheduler uiScheduler,
                                     @Nonnull @NetworkScheduler Scheduler networkScheduler,
                                     @NonNull ApiService apiservice,
                                     @NonNull @ForActivity final Context context,
                                     @NonNull UserPreferences userPreferences,
                                     @NonNull BookmarksDao bookmarksDao,
                                     @NonNull BookmarkHelper helper) {
        mHelper = helper;
        final boolean isNormalUser = userPreferences.isNormalUser();
        final BaseProfile currentUser = userPreferences.getUserOrPage();
        final String currentUserName = currentUser != null ? currentUser.getUsername() : null;

        final Observable<ResponseOrError<ShoutsResponse>> requestObservable = apiservice.getBookmarkedShouts()
                .observeOn(uiScheduler)
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.toResponseOrErrorObservable())
                .compose(ObservableExtensions.behaviorRefCount());

        adapterItemsObservable = requestObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .doOnNext(shoutsResponse -> {
                    final ImmutableMap.Builder<String, Boolean> builder = ImmutableMap.builder();
                    for (Shout shout : shoutsResponse.getShouts()) {
                        builder.put(shout.getId(), shout.isBookmarked());
                    }
                    bookmarksDao.updateBookmarkMap(builder.build());
                })
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        if (shoutsResponse.getShouts().isEmpty()) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataTextAdapterItem(context.getString(R.string.bookmarks_no_bookmarks)));
                        } else {
                            final Iterable<Shout> shouts = Iterables.filter(shoutsResponse.getShouts(), input -> {
                                assert input != null;
                                return input.getProfile() != null;
                            });
                            final Iterable<BaseAdapterItem> baseAdapterItems = Iterables.transform(shouts,
                                    (Function<Shout, BaseAdapterItem>) shout -> {
                                        final boolean isShoutOwner = shout.getProfile().getUsername().equals(currentUserName);
                                        final BookmarkHelper.ShoutItemBookmarkHelper shoutItemBookmarkHelper = helper.getShoutItemBookmarkHelper();
                                        return new ShoutAdapterItem(shout, isShoutOwner, isNormalUser,
                                                context, shoutSelectedSubject,
                                                PromotionHelper.promotionInfoOrNull(shout),
                                                bookmarksDao.getBookmarkForShout(shout.getId(), shout.isBookmarked()),
                                                shoutItemBookmarkHelper.getObserver(), shoutItemBookmarkHelper.getEnableObservable());
                                    });
                            return ImmutableList.copyOf(baseAdapterItems);
                        }
                    }
                });

        errorObservable = requestObservable
                .compose(ResponseOrError.<ShoutsResponse>onlyError());

        progressObservable = requestObservable
                .map(Functions1.returnFalse())
                .startWith(true);
    }

    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedSubject;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return adapterItemsObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Override
    @NonNull
    public Observable<String> getBookmarkSuccessMessage() {
        return mHelper.getBookmarkSuccessMessage();
    }
}
