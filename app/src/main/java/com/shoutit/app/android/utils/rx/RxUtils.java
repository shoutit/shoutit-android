package com.shoutit.app.android.utils.rx;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.widget.Spinner;

import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.ColoredSnackBar;

import rx.Observable;
import rx.functions.Action1;

public class RxUtils {

    public static Observable<OnItemClickEvent> spinnerItemClicks(final Spinner spinner) {
        return Observable.create(new OnSubscribeSpinnerOnItemClick(spinner));
    }

    @NonNull
    public static Action1<String> listenMessageAction(@NonNull final Activity activity) {
        return new Action1<String>() {
            @Override
            public void call(String name) {
                ColoredSnackBar
                        .success(ColoredSnackBar.contentView(activity), activity.getString(R.string.listen_success, name), Snackbar.LENGTH_SHORT)
                        .show();
            }
        };
    }

    @NonNull
    public static Action1<String> unListenMessageAction(@NonNull final Activity activity) {
        return new Action1<String>() {
            @Override
            public void call(String name) {
                ColoredSnackBar
                        .success(ColoredSnackBar.contentView(activity), activity.getString(R.string.unlisten_success, name), Snackbar.LENGTH_SHORT)
                        .show();
            }
        };
    }
}
