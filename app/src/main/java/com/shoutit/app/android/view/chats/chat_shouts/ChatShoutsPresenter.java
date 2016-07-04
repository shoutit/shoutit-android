package com.shoutit.app.android.view.chats.chat_shouts;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.utils.BookmarkHelper;
import com.shoutit.app.android.utils.PromotionHelper;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ChatShoutsPresenter {

    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();

    @Nonnull
    private final Observable<List<BaseAdapterItem>> adapterItemsObservable;
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final ShoutsDao shoutsDao;
    @Nonnull
    private final String conversationId;
    @Nonnull
    private final Observable<Integer> resultsCountObservable;

    public ChatShoutsPresenter(@Nonnull @UiScheduler Scheduler uiScheduler,
                               @Nonnull ShoutsDao shoutsDao,
                               @Nonnull String conversationId,
                               @NonNull @ForActivity final Context context,
                               @NonNull UserPreferences userPreferences,
                               @NonNull BookmarksDao bookmarksDao,
                               @NonNull BookmarkHelper helper) {
        this.shoutsDao = shoutsDao;
        this.conversationId = conversationId;

        final boolean isNormalUser = userPreferences.isNormalUser();
        final BaseProfile currentUser = userPreferences.getUserOrPage();
        final String currentUserName = currentUser != null ? currentUser.getUsername() : null;

        final Observable<ResponseOrError<ShoutsResponse>> requestObservable = shoutsDao
                .getConversationsShoutsDao(conversationId)
                .getShoutsObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        adapterItemsObservable = requestObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        if (shoutsResponse.getShouts().isEmpty()) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataTextAdapterItem(context.getString(R.string.chat_shouts_no_results)));
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

        resultsCountObservable = requestObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(ShoutsResponse::getCount);

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

    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(
                shoutsDao.getConversationsShoutsDao(conversationId)
                        .getLoadMoreObserver());
    }

    public Observable<Integer> getCountObservable() {
        return resultsCountObservable;
    }
}
