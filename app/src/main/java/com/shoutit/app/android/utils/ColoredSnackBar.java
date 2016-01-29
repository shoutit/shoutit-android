package com.shoutit.app.android.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.shoutit.app.android.R;

import javax.annotation.Nonnull;

import rx.functions.Action1;

public class ColoredSnackBar {

    @Nonnull
    private static View getSnackBarLayout(@Nonnull Snackbar snackbar) {
        return snackbar.getView();
    }

    @Nonnull
    public static Snackbar colorSnackBar(@Nonnull View view, @Nonnull CharSequence text, int length, int color) {
        Snackbar snackbar = Snackbar.make(view, text, length);
        getSnackBarLayout(snackbar).setBackgroundColor(color);
        return snackbar;
    }

    @Nonnull
    public static Snackbar colorSnackBar(@Nonnull View view, int text, int length, int color) {
        Snackbar snackbar = Snackbar.make(view, text, length);
        getSnackBarLayout(snackbar).setBackgroundColor(color);
        return snackbar;
    }

    @Nonnull
    public static Snackbar success(@Nonnull View view, int text, int length) {
        return colorSnackBar(view, text, length, view.getResources().getColor(R.color.snackbar_success));
    }

    @Nonnull
    public static Snackbar success(@Nonnull View view, @Nonnull CharSequence text, int length) {
        return colorSnackBar(view, text, length, view.getResources().getColor(R.color.snackbar_success));
    }

    @Nonnull
    public static Snackbar error(@Nonnull View view, int text, int length) {
        return colorSnackBar(view, text, length, view.getResources().getColor(R.color.snackbar_error));
    }

    @Nonnull
    public static Snackbar error(@Nonnull View view, @Nonnull CharSequence text, int length) {
        return colorSnackBar(view, text, length, view.getResources().getColor(R.color.snackbar_error));
    }

    @Nonnull
    public static View contentView(@Nonnull Activity activity) {
        return activity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

    @NonNull
    public static Action1<Object> errorSnackBarAction(final View contentView, final int resId) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                ColoredSnackBar.error(contentView, resId, Snackbar.LENGTH_SHORT).show();
            }
        };
    }

    @NonNull
    public static Action1<Object> successSnackBarAction(final View contentView, final int resId) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                ColoredSnackBar.success(contentView, resId, Snackbar.LENGTH_SHORT).show();
            }
        };
    }
}
