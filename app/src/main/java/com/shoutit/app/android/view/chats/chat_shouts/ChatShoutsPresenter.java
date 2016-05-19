package com.shoutit.app.android.view.chats.chat_shouts;

import android.content.Context;

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
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.utils.PreferencesHelper;
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
    private Observable<Integer> resultsCountObservable;

    public ChatShoutsPresenter(@Nonnull @UiScheduler Scheduler uiScheduler,
                               @Nonnull ShoutsDao shoutsDao,
                               @Nonnull String conversationId,
                               @ForActivity final Context context,
                               UserPreferences userPreferences) {
        this.shoutsDao = shoutsDao;
        this.conversationId = conversationId;

        final boolean isNormalUser = userPreferences.isNormalUser();
        final User currentUser = userPreferences.getUser();
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
                            return Lists.transform(shoutsResponse.getShouts(),
                                    new Function<Shout, BaseAdapterItem>() {
                                        @Override
                                        public ShoutAdapterItem apply(Shout shout) {
                                            final boolean isShoutOwner = shout.getProfile().getUsername().equals(currentUserName);
                                            return new ShoutAdapterItem(shout, isShoutOwner, isNormalUser, context, shoutSelectedSubject);
                                        }
                                    });
                        }
                    }
                });

        resultsCountObservable = requestObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, Integer>() {
                    @Override
                    public Integer call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getCount();
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

    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(
                shoutsDao.getConversationsShoutsDao(conversationId)
                        .getLoadMoreObserver());
    }

    public Observable<Integer> getCountObservable() {
        return resultsCountObservable;
    }
}
