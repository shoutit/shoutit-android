package com.shoutit.app.android.view.conversations;

import rx.Observable;
import rx.subjects.PublishSubject;

public class RefreshConversationBus {

    private final PublishSubject<Object> refreshConversations = PublishSubject.create();

    public Observable<Object> getRefreshConversationBus() {
        return refreshConversations;
    }

    public void post() {
        refreshConversations.onNext(new Object());
    }

}
