package com.shoutit.app.android.view.shout;

import android.content.Context;
import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.model.RelatedShoutsPointer;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.subjects.PublishSubject;

public class ShoutPresenter {

    private static final int USER_SHOUTS_PAGE_SIZE = 4;
    private static final int RELATED_SHOUTS_PAGE_SIZE = 6;

    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> progressObservable;

    private PublishSubject<String> addToCartSubject = PublishSubject.create();
    private PublishSubject<String> userShoutSelectedSubject = PublishSubject.create();
    private PublishSubject<String> relatedShoutSelectedSubject = PublishSubject.create();
    private PublishSubject<String> visitProfileSubject = PublishSubject.create();

    @Inject
    public ShoutPresenter(@Nonnull final ShoutsDao shoutsDao,
                          @Nonnull String shoutId,
                          @Nonnull @ForActivity final Context context) {

        /** Requests **/
        final Observable<Shout> shoutResponse = shoutsDao.getShoutObservable(shoutId)
                .compose(ResponseOrError.<Shout>onlySuccess());

        final Observable<String> userNameObservable = shoutResponse
                .map(new Func1<Shout, String>() {
                    @Override
                    public String call(Shout shout) {
                        return shout.getUser().getName();
                    }
                })
                .compose(ObservableExtensions.<String>behaviorRefCount());

        final Observable<ResponseOrError<ShoutsResponse>> userShoutsObservable = userNameObservable
                .switchMap(new Func1<String, Observable<ResponseOrError<ShoutsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ShoutsResponse>> call(String userName) {
                        return shoutsDao.getUserShoutObservable(new UserShoutsPointer(USER_SHOUTS_PAGE_SIZE, userName));
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<Shout>> successUserShoutsObservable = userShoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<Shout>>() {
                    @Override
                    public List<Shout> call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getShouts();
                    }
                })
                .filter(Functions1.isNotNull());

        final Observable<List<Shout>> relatedShoutsObservable = shoutsDao
                .getRelatedShoutsObservable(new RelatedShoutsPointer(shoutId, RELATED_SHOUTS_PAGE_SIZE))
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<Shout>>() {
                    @Override
                    public List<Shout> call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getShouts();
                    }
                })
                .filter(Functions1.isNotNull());


        /** Adapter Items **/
        final Observable<ShoutAdapterItems.ShoutAdapterItem> shoutItemObservable =
                shoutResponse.map(new Func1<Shout, ShoutAdapterItems.ShoutAdapterItem>() {
                    @Override
                    public ShoutAdapterItems.ShoutAdapterItem call(Shout shout) {
                        return new ShoutAdapterItems.ShoutAdapterItem(addToCartSubject, shout);
                    }
                });

        final Observable<List<BaseAdapterItem>> userShoutItemsObservable =
                successUserShoutsObservable.map(new Func1<List<Shout>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Shout> shouts) {
                        final List<BaseAdapterItem> items =
                                Lists.transform(shouts, new Function<Shout, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public ShoutAdapterItems.UserShoutAdapterItem apply(@Nullable Shout input) {
                                        return new ShoutAdapterItems.UserShoutAdapterItem(input, userShoutSelectedSubject);
                                    }
                                });

                        return ImmutableList.copyOf(items);
                    }
                });

        final Observable<List<BaseAdapterItem>> relatedShoutsItems = relatedShoutsObservable
                .map(new Func1<List<Shout>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Shout> shouts) {
                        final List<BaseAdapterItem> items =
                                Lists.transform(shouts, new Function<Shout, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public BaseAdapterItem apply(@Nullable Shout input) {
                                        return new ShoutAdapterItem(input, context, relatedShoutSelectedSubject);
                                    }
                                });

                        return ImmutableList.copyOf(items);
                    }
                });

        allAdapterItemsObservable = Observable.combineLatest(
                shoutItemObservable,
                userShoutItemsObservable.startWith(ImmutableList.<BaseAdapterItem>of()),
                relatedShoutsItems.startWith(ImmutableList.<BaseAdapterItem>of()),
                new Func3<ShoutAdapterItems.ShoutAdapterItem, List<BaseAdapterItem>, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutAdapterItems.ShoutAdapterItem shout,
                                                      List<BaseAdapterItem> userShouts,
                                                      List<BaseAdapterItem> relatedShouts) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        builder.add(shout);

                        final String userName = shout.getShout().getUser().getName();
                        if (!userShouts.isEmpty()) {
                            builder.add(new ShoutAdapterItems.HeaderAdapterItem(context.getString(R.string.shout_user_shouts_header, userName)))
                                    .addAll(userShouts);
                        }

                        builder.add(new ShoutAdapterItems.ViewProfileAdapterItem(visitProfileSubject, userName));

                        if (!relatedShouts.isEmpty()) {
                            builder.add(new ShoutAdapterItems.HeaderAdapterItem(context.getString(R.string.shout_related_shouts_header)))
                                    .addAll(relatedShouts);
                        }

                        return builder.build();
                    }
                });

        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(userShoutsObservable),
                ResponseOrError.transform(shoutsDao.getShoutObservable(shoutId)),
                ResponseOrError.transform(shoutsDao.getRelatedShoutsObservable(new RelatedShoutsPointer(shoutId, RELATED_SHOUTS_PAGE_SIZE)))))
                .filter(Functions1.isNotNull());

        /** Progress **/
        progressObservable = Observable.merge(
                errorObservable.map(Functions1.returnFalse()),
                allAdapterItemsObservable.map(Functions1.returnFalse()))
                .startWith(true);
    }

    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }
}


