package com.shoutit.app.android.view.postsignup;

import com.shoutit.app.android.utils.rx.RxMoreObservers;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

public class PostSignUpBus {

    private final PublishSubject<Object> nextClickSubject = PublishSubject.create();
    private final PublishSubject<Object> interestsUploadedSubject = PublishSubject.create();

    public PostSignUpBus() {
    }

    public void nextClicked() {
        nextClickSubject.onNext(null);
    }

    public Observable<Object> getNextClickObservable() {
        return nextClickSubject;
    }

    public Observable<Object> getInterestsUploadedObservable() {
        return interestsUploadedSubject;
    }

    public Observer<Object> getInterestsUploadedObserver() {
        return RxMoreObservers.ignoreCompleted(interestsUploadedSubject);
    }
}
