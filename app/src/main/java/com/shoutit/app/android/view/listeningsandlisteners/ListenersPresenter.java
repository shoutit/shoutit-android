package com.shoutit.app.android.view.listeningsandlisteners;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;

public class ListenersPresenter extends ListeningsAndListenersPresenter {

    @Nullable
    private final String userName;
    @Nonnull
    private final ListenersDaos listenersDao;

    public ListenersPresenter(@Nullable String userName,
                              @Nonnull ListenersDaos listenersDao,
                              @UiScheduler Scheduler uiScheduler) {
        super(uiScheduler);
        this.userName = userName;
        this.listenersDao = listenersDao;
    }

    @Override
    public void refreshData() {
        listenersDao.getDao(userName).getRefreshSubject().onNext(null);
    }

    @Override
    public Observable<ResponseOrError<ListeningResponse>> getRequestObservable() {
        return listenersDao.getDao(userName).getLstenersObservable();
    }

    @Override
    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(listenersDao.getDao(userName).getLoadMoreObserver());
    }
}
