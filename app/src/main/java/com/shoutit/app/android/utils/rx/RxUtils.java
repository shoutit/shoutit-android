package com.shoutit.app.android.utils.rx;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.widget.Spinner;

import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.utils.ColoredSnackBar;

import rx.Observable;
import rx.functions.Action1;

public class RxUtils {

    public static Observable<OnItemClickEvent> spinnerItemClicks(final Spinner spinner) {
        return Observable.create(new OnSubscribeSpinnerOnItemClick(spinner));
    }

    @NonNull
    public static Action1<ApiMessageResponse> apiMessageAction(@NonNull final Activity activity) {
        return apiMessageResponse -> ColoredSnackBar
                .success(ColoredSnackBar.contentView(activity), apiMessageResponse.getSuccess(), Snackbar.LENGTH_SHORT)
                .show();
    }
}
