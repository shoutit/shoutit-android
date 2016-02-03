package com.shoutit.app.android.utils;

import android.support.v7.widget.RecyclerView;

import javax.annotation.Nonnull;

import rx.functions.Func1;

public class LoadMoreHelper {

    @Nonnull
    public static Func1<Object, Boolean> needLoadMore(@Nonnull final MyLayoutManager layoutManager,
                                                      @Nonnull final RecyclerView.Adapter<?> adapter) {
        return new Func1<Object, Boolean>() {
            @Override
            public Boolean call(final Object recyclerScrollEvent) {
                return isNeedLoadMore(layoutManager, adapter);
            }
        };
    }

    private static boolean isNeedLoadMore(@Nonnull MyLayoutManager layoutManager,
                                          @Nonnull RecyclerView.Adapter<?> adapter) {
        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        if (firstVisibleItemPosition == RecyclerView.NO_POSITION ||
                lastVisibleItemPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        final int countsOnPage = lastVisibleItemPosition - firstVisibleItemPosition;
        return lastVisibleItemPosition + countsOnPage + 1 >= adapter.getItemCount();
    }
}