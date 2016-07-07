package com.shoutit.app.android.view.shouts_list_common;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.common.base.Strings;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.shout.ShoutActivity;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import rx.subscriptions.CompositeSubscription;

public class ShoutListActivityHelper {

    public static CompositeSubscription setup(RxAppCompatActivity activity, ShoutListPresenter presenter, SimpleShoutsAdapter adapter, View progressBar) {
        return new CompositeSubscription(
                presenter.getAdapterItemsObservable()
                        .subscribe(adapter),
                presenter.getProgressObservable()
                        .subscribe(RxView.visibility(progressBar)),
                presenter.getBookmarkSuccessMessage()
                        .subscribe(ColoredSnackBar.successSnackBarAction(ColoredSnackBar.contentView(activity))),
                presenter.getErrorObservable()
                        .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(activity))),
                presenter.getShoutSelectedObservable()
                        .subscribe(shoutId -> {
                            if (Strings.isNullOrEmpty(shoutId)) {
                                ColoredSnackBar.error(ColoredSnackBar.contentView(activity), activity.getString(R.string.shout_deleted), Snackbar.LENGTH_SHORT).show();
                            } else {
                                activity.startActivity(ShoutActivity.newIntent(activity, shoutId));
                            }
                        }));
    }
}
