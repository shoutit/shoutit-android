package com.shoutit.app.android.view.notifications;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.NotificationsResponse;
import com.shoutit.app.android.dao.NotificationsDao;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class NotificationsPresenter {

    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> adapterItemsObservable;

    @Nonnull
    private final PublishSubject<String> openViewForNotification = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> markAllAsReadSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> markSingleAsReadSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> markNotificationAsReadSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    @Nonnull
    private final NotificationsDao dao;

    @Inject
    public NotificationsPresenter(@Nonnull NotificationsDao dao,
                                  @Nonnull @UiScheduler final Scheduler uiScheduler,
                                  @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                  @Nonnull final ApiService apiService) {
        this.dao = dao;

        final Observable<ResponseOrError<NotificationsResponse>> notificationsObservable = dao
                .getNotificationsObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<NotificationsResponse>>behaviorRefCount());

        final Observable<NotificationsResponse> successNotificationsObservable =
                notificationsObservable.compose(ResponseOrError.<NotificationsResponse>onlySuccess());

        adapterItemsObservable = successNotificationsObservable
                .map(new Func1<NotificationsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(NotificationsResponse notificationsResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = new ImmutableList.Builder<>();

                        if (!notificationsResponse.getResults().isEmpty()) {
                            builder.addAll(Lists.transform(notificationsResponse.getResults(),
                                    new Function<NotificationsResponse.Notification, BaseAdapterItem>() {
                                        @Override
                                        public BaseAdapterItem apply(NotificationsResponse.Notification input) {
                                            return new NotificationAdapterItem(input, openViewForNotification, markSingleAsReadSubject);
                                        }
                                    }));
                            builder.add(new NoDataAdapterItem());
                        }

                        return builder.build();
                    }
                });

        final Observable<ResponseOrError<ResponseBody>> markAllAsReadObservable = markAllAsReadSubject
                .switchMap(new Func1<Object, Observable<ResponseOrError<ResponseBody>>>() {
                    @Override
                    public Observable<ResponseOrError<ResponseBody>> call(Object o) {
                        return apiService.markAllAsRead()
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ResponseBody>>behaviorRefCount());

        markAllAsReadObservable.compose(ResponseOrError.<ResponseBody>onlySuccess())
                .subscribe(dao.getRefreshObserver());

        markSingleAsReadSubject
                .switchMap(new Func1<String, Observable<ResponseOrError<ResponseBody>>>() {
                    @Override
                    public Observable<ResponseOrError<ResponseBody>> call(final String notificationId) {
                        return apiService.markAsRead(notificationId)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable())
                                .doOnNext(new Action1<ResponseOrError<ResponseBody>>() {
                                    @Override
                                    public void call(ResponseOrError<ResponseBody> responseOrError) {
                                        if (responseOrError.isData()) {
                                            markNotificationAsReadSubject.onNext(notificationId);
                                        } else {
                                            errorSubject.onNext(responseOrError.error());
                                        }
                                    }
                                });
                    }
                })
                .subscribe();

        markNotificationAsReadSubject
                .withLatestFrom(successNotificationsObservable, new Func2<String, NotificationsResponse, NotificationsResponse>() {
                    @Override
                    public NotificationsResponse call(String notificationId, NotificationsResponse lastResponse) {
                        final List<NotificationsResponse.Notification> updatedList = new ArrayList<>(lastResponse.getResults());

                        for (int i = 0; i < lastResponse.getResults().size(); i++) {
                            if (lastResponse.getResults().get(i).getId().equals(notificationId)) {
                                updatedList.set(i, lastResponse.getResults().get(i).markAsRead());
                                break;
                            }
                        }

                        return new NotificationsResponse(lastResponse.getCount(),
                                lastResponse.getNext(), lastResponse.getPrevious(), updatedList);
                    }
                })
                .subscribe(dao.getLoadMoreObserver());

        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(notificationsObservable),
                        ResponseOrError.transform(markAllAsReadObservable)
                ))
                .mergeWith(errorSubject)
                .filter(Functions1.isNotNull());

        progressObservable = Observable.merge(
                notificationsObservable.map(Functions1.returnFalse()),
                markAllAsReadSubject.map(Functions1.returnTrue()),
                errorObservable.map(Functions1.returnFalse()))
                .startWith(true);
    }

    @Nonnull
    public Observer<NotificationsResponse> loadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(dao.getLoadMoreObserver());
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return adapterItemsObservable;
    }

    @Nonnull
    public Observable<String> getOpenViewForNotificationObservable() {
        return openViewForNotification;
    }

    public void markAllNotificationsAsRead() {
        markAllAsReadSubject.onNext(null);
    }

    public class NotificationAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final NotificationsResponse.Notification notification;
        @Nonnull
        private final Observer<String> openViewForNotification;
        @Nonnull
        private final Observer<String> markSingleAsReadSubject;

        public NotificationAdapterItem(@Nonnull NotificationsResponse.Notification notification,
                                       @Nonnull Observer<String> openViewForNotification,
                                       @Nonnull Observer<String> markSingleAsReadSubject) {
            this.notification = notification;
            this.openViewForNotification = openViewForNotification;
            this.markSingleAsReadSubject = markSingleAsReadSubject;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof NotificationAdapterItem
                    && notification.getId().equals(((NotificationAdapterItem) item).notification.getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof NotificationAdapterItem
                    && notification.isRead() == (((NotificationAdapterItem) item).notification.isRead());
        }

        @Nonnull
        public NotificationsResponse.Notification getNotification() {
            return notification;
        }

        private void markNotificationAsRead() {
            if (!notification.isRead()) {
                markSingleAsReadSubject.onNext(notification.getId());
            }
        }

        public void onNotificationClicked() {
            markNotificationAsRead();
            openViewForNotification.onNext(notification.getDisplay().getAppUrl());
        }

        public NotificationsResponse.DisplayInfo getDisplayInfo() {
            return notification.getDisplay();
        }
    }
}
