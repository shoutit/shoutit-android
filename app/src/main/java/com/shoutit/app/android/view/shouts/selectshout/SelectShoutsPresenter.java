package com.shoutit.app.android.view.shouts.selectshout;

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
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.utils.BookmarkHelper;
import com.shoutit.app.android.utils.PromotionHelper;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class SelectShoutsPresenter {

    private final Observable<List<BaseAdapterItem>> mListObservable;
    private final Observable<Throwable> mThrowableObservable;
    private final Observable<Boolean> mProgressObservable;

    @Nonnull
    private final PublishSubject<String> shoutSelectedObserver = PublishSubject.create();
    @NonNull
    private final BookmarkHelper mBookmarkHelper;

    @Inject
    public SelectShoutsPresenter(@NetworkScheduler Scheduler networkScheduler,
                                 @UiScheduler Scheduler uiScheduler,
                                 ApiService apiService,
                                 UserPreferences userPreferences,
                                 @ForActivity final Context context,
                                 @NonNull BookmarksDao bookmarksDao,
                                 @NonNull BookmarkHelper bookmarkHelper) {
        mBookmarkHelper = bookmarkHelper;
        final BaseProfile user = userPreferences.getUserOrPage();
        assert user != null;
        final Observable<ResponseOrError<ShoutsResponse>> shoutsResponse = apiService.shoutsForUser(user.getUsername(), 0, 100)
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ResponseOrError.<ShoutsResponse>toResponseOrErrorObservable())
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        mListObservable = shoutsResponse.compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        return ImmutableList.copyOf(Iterables.transform(shoutsResponse.getShouts(), new Function<Shout, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(Shout input) {
                                final BookmarkHelper.ShoutItemBookmarkHelper shoutItemBookmarkHelper = bookmarkHelper.getShoutItemBookmarkHelper();
                                return new ShoutAdapterItem(input, false, false, context, shoutSelectedObserver,
                                        PromotionHelper.promotionInfoOrNull(input),
                                        bookmarksDao.getBookmarkForShout(input.getId(), input.isBookmarked()),
                                        shoutItemBookmarkHelper.getObserver(), shoutItemBookmarkHelper.getEnableObservable());
                            }
                        }));
                    }
                });

        mThrowableObservable = shoutsResponse.compose(ResponseOrError.<ShoutsResponse>onlyError());
        mProgressObservable = shoutsResponse.map(Functions1.returnFalse()).startWith(true);
    }


    @NonNull
    public Observable<List<BaseAdapterItem>> getSuccessObservable() {
        return mListObservable;
    }

    @NonNull
    public Observable<String> getBookmarkSuccessMessage() {
        return mBookmarkHelper.getBookmarkSuccessMessage();
    }

    @NonNull
    public Observable<Throwable> getFailObservable() {
        return mThrowableObservable;
    }

    @NonNull
    public Observable<Boolean> getProgressVisible() {
        return mProgressObservable;
    }

    @Nonnull
    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedObserver;
    }
}