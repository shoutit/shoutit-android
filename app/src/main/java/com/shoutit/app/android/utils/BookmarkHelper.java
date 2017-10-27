package com.shoutit.app.android.utils;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.dao.BookmarksDao;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.observers.Observers;
import rx.subjects.PublishSubject;

public class BookmarkHelper {

    private final ApiService apiService;
    private final BookmarksDao bookmarksDao;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;

    @NonNull
    private final PublishSubject<String> mBookmarkSuccessMessage = PublishSubject.create();

    @Inject
    public BookmarkHelper(ApiService apiService, BookmarksDao bookmarksDao, @NetworkScheduler Scheduler networkScheduler, @UiScheduler Scheduler uiScheduler) {
        this.apiService = apiService;
        this.bookmarksDao = bookmarksDao;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
    }

    public ShoutItemBookmarkHelper getShoutItemBookmarkHelper() {
        final PublishSubject<Boolean> enableObserable = PublishSubject.create();
        return new ShoutItemBookmarkHelper(getObserver(enableObserable, mBookmarkSuccessMessage), enableObserable);
    }

    private Observer<Pair<String, Boolean>> getObserver(PublishSubject<Boolean> enableObserable, Observer<String> bookmarkSuccessMessageObserver) {
        return Observers.create(new Action1<Pair<String, Boolean>>() {
            @Override
            public void call(Pair<String, Boolean> stringBooleanPair) {
                enableObserable.onNext(false);
                final Observable<ApiMessageResponse> observable;
                if (stringBooleanPair.second) {
                    observable = apiService.markAsBookmark(stringBooleanPair.first);
                } else {
                    observable = apiService.deleteBookmark(stringBooleanPair.first);
                }
                observable
                        .observeOn(mUiScheduler)
                        .subscribeOn(mNetworkScheduler)
                        .subscribe(responseBody -> {
                            bookmarksDao.updateBookmark(stringBooleanPair.first, stringBooleanPair.second);
                            enableObserable.onNext(true);
                            bookmarkSuccessMessageObserver.onNext(responseBody.getSuccess());
                        }, throwable -> {
                            bookmarksDao.updateBookmark(stringBooleanPair.first, !stringBooleanPair.second);
                            enableObserable.onNext(true);
                        });
            }
        });
    }

    @NonNull
    public Observable<String> getBookmarkSuccessMessage() {
        return mBookmarkSuccessMessage;
    }

    public static class ShoutItemBookmarkHelper {
        private final Observer<Pair<String, Boolean>> mObserver;
        private final Observable<Boolean> enableObserable;

        public ShoutItemBookmarkHelper(Observer<Pair<String, Boolean>> observer, Observable<Boolean> enableObserable) {
            this.mObserver = observer;
            this.enableObserable = enableObserable;
        }

        public Observer<Pair<String, Boolean>> getObserver() {
            return mObserver;
        }

        public Observable<Boolean> getEnableObservable() {
            return enableObserable;
        }
    }
}
