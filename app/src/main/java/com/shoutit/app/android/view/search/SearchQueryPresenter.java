package com.shoutit.app.android.view.search;

import com.appunite.rx.functions.Functions1;

import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class SearchQueryPresenter {

    private final BehaviorSubject<String> querySubject = BehaviorSubject.create();
    private final PublishSubject<String> querySubmittedSubject = PublishSubject.create();
    private final PublishSubject<String> fillSearchWithSuggestionSubject = PublishSubject.create();

    private final Observable<String> querySubmittedObservable;
    private final Observable<String> emptyQuerySubmittedObservable;

    public SearchQueryPresenter() {

        querySubmittedObservable = querySubmittedSubject
                .filter(Functions1.neg(Functions1.isNullOrEmpty()));

        emptyQuerySubmittedObservable = querySubmittedSubject
                .filter(Functions1.isNullOrEmpty());
    }

    public BehaviorSubject<String> getQuerySubject() {
        return querySubject;
    }

    public PublishSubject<String> getQuerySubmittedSubject() {
        return querySubmittedSubject;
    }

    public Observable<String> getQuerySubmittedObservable() {
        return querySubmittedObservable;
    }

    public Observable<String> getEmptyQuerySubmittedObservable() {
        return emptyQuerySubmittedObservable;
    }

    public Observer<String> getFillSearchWithSuggestionObserver() {
        return fillSearchWithSuggestionSubject;
    }

    public Observable<String> getFillSearchWithSuggestionObservable() {
        return fillSearchWithSuggestionSubject;
    }
}
