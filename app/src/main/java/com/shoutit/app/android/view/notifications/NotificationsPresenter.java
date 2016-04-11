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
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.NotificationsResponse;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.dao.NotificationsDao;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class NotificationsPresenter {

    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> adapterItemsObservable;

    @Nonnull
    private final PublishSubject<String> openUserOrPageProfileSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> openTagProfileSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> markAllAsReadSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> markSingleAsReadSubject = PublishSubject.create();

    @Inject
    public NotificationsPresenter(@Nonnull NotificationsDao dao,
                                  @Nonnull @UiScheduler final Scheduler uiScheduler,
                                  @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                  @Nonnull final ApiService apiService) {

        final Observable<ResponseOrError<NotificationsResponse>> notificationsObservable = dao
                .getNotificationsObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<NotificationsResponse>>behaviorRefCount());

        adapterItemsObservable = notificationsObservable
                .compose(ResponseOrError.<NotificationsResponse>onlySuccess())
                .map(new Func1<NotificationsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(NotificationsResponse notificationsResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = new ImmutableList.Builder<>();

                        if (!notificationsResponse.getResults().isEmpty()) {
                            builder.addAll(Lists.transform(notificationsResponse.getResults(),
                                    new Function<NotificationsResponse.Notification, BaseAdapterItem>() {
                                        @Override
                                        public BaseAdapterItem apply(NotificationsResponse.Notification input) {
                                            return new NotificationAdapterItem(input, openUserOrPageProfileSubject,
                                                    openTagProfileSubject, markSingleAsReadSubject);
                                        }
                                    }));
                            builder.add(new NoDataAdapterItem());
                        }

                        return builder.build();
                    }
                });

        final Observable<ResponseOrError<ResponseBody>> markAsReadObservable = markAllAsReadSubject
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

        markAsReadObservable.compose(ResponseOrError.<ResponseBody>onlySuccess())
                .subscribe(dao.getRefreshObserver());

        final Observable<ResponseOrError<ResponseBody>> markSingleAsReadObservable = markSingleAsReadSubject
                .switchMap(new Func1<String, Observable<ResponseOrError<ResponseBody>>>() {
                    @Override
                    public Observable<ResponseOrError<ResponseBody>> call(String notificationId) {
                        return apiService.markAsRead(notificationId)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ResponseBody>>behaviorRefCount());

        markSingleAsReadObservable
                .compose(ResponseOrError.<ResponseBody>onlySuccess())
                .subscribe(dao.getRefreshObserver());

        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(notificationsObservable),
                        ResponseOrError.transform(markAsReadObservable)
                ))
                .filter(Functions1.isNotNull());

        progressObservable = Observable.merge(
                notificationsObservable.map(Functions1.returnFalse()),
                markAllAsReadSubject.map(Functions1.returnTrue()),
                errorObservable.map(Functions1.returnFalse()))
                .startWith(true);
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
    public Observable<String> getOpenUserOrPageProfileObservable() {
        return openUserOrPageProfileSubject;
    }

    @Nonnull
    public Observable<String> getOpenTagProfileObservable() {
        return openTagProfileSubject;
    }

    public void markAllNotificationsAsRead() {
        markAllAsReadSubject.onNext(null);
    }

    public class NotificationAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final NotificationsResponse.Notification notification;
        @Nonnull
        private final Observer<String> openUserOrPageProfileObserver;
        @Nonnull
        private final Observer<String> openTagProfileObserver;
        @Nonnull
        private final Observer<String> markSingleAsReadSubject;

        public NotificationAdapterItem(@Nonnull NotificationsResponse.Notification notification,
                                       @Nonnull Observer<String> openUserOrPageProfileObserver,
                                       @Nonnull Observer<String> openTagProfileObserver,
                                       @Nonnull Observer<String> markSingleAsReadSubject) {
            this.notification = notification;
            this.openUserOrPageProfileObserver = openUserOrPageProfileObserver;
            this.openTagProfileObserver = openTagProfileObserver;
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

        public void openProfileAndMarkAsRead() {
            final BaseProfile profile = notification.getAttachedObject().getProfile();
            if (profile == null) {
                return;
            }

            if (ProfileType.USER.equals(profile.getType()) || ProfileType.PAGE.equals(profile.getType())) {
                openUserOrPageProfileObserver.onNext(profile.getUsername());
                markNotificationAsRead();
            } else if (ProfileType.TAG.equals(profile.getType())) {
                openTagProfileObserver.onNext(profile.getUsername());
                markNotificationAsRead();
            }
        }

        public void markNotificationAsRead() {
            markSingleAsReadSubject.onNext(notification.getId());
        }
    }
}
