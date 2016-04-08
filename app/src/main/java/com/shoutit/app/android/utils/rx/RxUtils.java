package com.shoutit.app.android.utils.rx;

import android.widget.Spinner;

import rx.Observable;

public class RxUtils {

    public static Observable<OnItemClickEvent> spinnerItemClicks(final Spinner spinner) {
        return Observable.create(new OnSubscribeSpinnerOnItemClick(spinner));
    }
}
