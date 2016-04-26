package com.shoutit.app.android.utils.stackcounter;

import android.support.annotation.NonNull;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.Functions1;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

public class StackCounter {

    public static final int DELAY = 5;

    int activtitiesRunning;
    private Subscription mSubscription = Subscriptions.empty();
    private final Scheduler mScheduler;
    private final PublishSubject<Boolean> subject = PublishSubject.create();

    @Inject
    public StackCounter(@NonNull @NetworkScheduler Scheduler scheduler) {
        mScheduler = scheduler;
    }

    public void onActivityResumed() {
        activtitiesRunning++;
        checkIfMovedToForeground();
    }

    private void checkIfMovedToForeground() {
        if (activtitiesRunning == 1) {
            mSubscription.unsubscribe();
            subject.onNext(true);
        }
    }

    public void onActivityPaused() {
        activtitiesRunning--;
        checkIfMovedToBackground();
    }

    private void checkIfMovedToBackground() {
        if (activtitiesRunning == 0) {
            mSubscription = Observable.timer(DELAY, TimeUnit.SECONDS, mScheduler)
                    .mergeWith(Observable.<Long>never())
                    .map(Functions1.returnFalse())
                    .subscribe(subject);
        }
    }

    @NonNull
    public Observable<Boolean> getSubject() {
        return subject.distinctUntilChanged();
    }
}
