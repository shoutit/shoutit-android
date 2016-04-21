package com.shoutit.app.android.view.listeningsandlisteners;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;

public class ListeningsPresenter extends ListeningsAndListenersPresenter {

    @Nonnull
    private final ListeningsDao listeningsDao;

    public ListeningsPresenter(@UiScheduler Scheduler uiScheduler,
                               @Nonnull ListeningsDao listeningsDao) {
        super(uiScheduler);
        this.listeningsDao = listeningsDao;
    }

    @Override
    public void refreshData() {
        listeningsDao.getRefreshSubject().onNext(null);
    }

    @Override
    public Observable<ResponseOrError<ListeningResponse>> getRequestObservable() {
        return listeningsDao.getLsteningObservable();
    }

    @Override
    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(listeningsDao.getLoadMoreObserver());
    }
}
