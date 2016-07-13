package com.shoutit.app.android.view.profile.page.edit;

import com.shoutit.app.android.api.model.Page;

import rx.Observable;

public interface PageDataProvider {

    Observable<Page> getPage();

}
