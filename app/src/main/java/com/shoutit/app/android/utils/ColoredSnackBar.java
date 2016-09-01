package com.shoutit.app.android.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ErrorHandler;

import javax.annotation.Nonnull;

import rx.functions.Action1;

public class ColoredSnackBar {

    @Nonnull
    private static View getSnackBarLayout(@Nonnull Snackbar snackbar) {
        return snackbar.getView();
    }

    @Nonnull
    public static Snackbar colorSnackBar(@Nonnull View view, @Nonnull CharSequence text, int length, int color) {
        final Snackbar snackbar = Snackbar.make(view, text, length);
        getSnackBarLayout(snackbar).setBackgroundColor(color);
        return snackbar;
    }

    @Nonnull
    public static Snackbar colorSnackBar(@Nonnull View view, @StringRes int text, int length, int color) {
        final Snackbar snackbar = Snackbar.make(view, text, length);
        getSnackBarLayout(snackbar).setBackgroundColor(color);
        return snackbar;
    }

    @Nonnull
    public static Snackbar success(@Nonnull View view, @StringRes int text, int length) {
        return colorSnackBar(view, text, length, view.getResources().getColor(R.color.snackbar_success));
    }

    @Nonnull
    public static Snackbar success(@Nonnull View view, @Nonnull CharSequence text, int length) {
        return colorSnackBar(view, text, length, view.getResources().getColor(R.color.snackbar_success));
    }

    @Nonnull
    public static Snackbar error(@Nonnull View view, @StringRes int text, int length) {
        return colorSnackBar(view, text, length, view.getResources().getColor(R.color.snackbar_error));
    }

    @Nonnull
    public static Snackbar error(@Nonnull View view, @Nonnull CharSequence text, int length) {
        return colorSnackBar(view, text, length, view.getResources().getColor(R.color.snackbar_error));
    }

    @Nonnull
    public static Snackbar error(@Nonnull View view, @Nonnull Throwable throwable, int length) {
        return colorSnackBar(view, ErrorHandler.getErrorMessage(throwable, view.getContext()), length, view.getResources().getColor(R.color.snackbar_error));
    }

    @Nonnull
    public static Snackbar error(@Nonnull View view, @Nonnull Throwable throwable) {
        return ColoredSnackBar.error(view, ErrorHandler.getErrorMessage(throwable, view.getContext()), Snackbar.LENGTH_LONG);
    }

    public static void showError(@Nonnull Activity activity, @Nonnull Throwable throwable) {
        error(contentView(activity), throwable).show();
    }

    @Nonnull
    public static View contentView(@Nonnull Activity activity) {
        return activity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

    @NonNull
    public static Action1<Object> errorSnackBarAction(final View contentView, @StringRes final int resId) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                ColoredSnackBar.error(contentView, resId, Snackbar.LENGTH_LONG).show();
            }
        };
    }

    @NonNull
    public static Action1<Throwable> errorSnackBarAction(final View contentView) {
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                ColoredSnackBar.error(contentView, ErrorHandler.getErrorMessage(throwable, contentView.getContext()), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    @NonNull
    public static Action1<Object> successSnackBarAction(final View contentView, @StringRes final int resId) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                ColoredSnackBar.success(contentView, resId, Snackbar.LENGTH_LONG).show();
            }
        };
    }

    @NonNull
    public static Action1<String> successSnackBarAction(final View contentView) {
        return new Action1<String>() {
            @Override
            public void call(String message) {
                ColoredSnackBar.success(contentView, message, Snackbar.LENGTH_LONG).show();
            }
        };
    }
}
