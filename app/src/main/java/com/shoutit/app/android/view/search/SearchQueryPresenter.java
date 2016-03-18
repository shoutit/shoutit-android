package com.shoutit.app.android.view.search;

import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class SearchQueryPresenter {

    public BehaviorSubject<String> querySubject = BehaviorSubject.create();
    public PublishSubject<String> getQuerySubmittedSubject = PublishSubject.create();

    public SearchQueryPresenter() {
    }

    public BehaviorSubject<String> getQuerySubject() {
        return querySubject;
    }

    public PublishSubject<String> getQuerySubmittedSubject() {
        return getQuerySubmittedSubject;
    }
}
