package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.subjects.BehaviorSubject;

@Singleton
public class BookmarksDao {

    private final Cache<String, BehaviorSubject<Boolean>> bookmarksCache;

    @Inject
    public BookmarksDao() {
        this.bookmarksCache = CacheBuilder.newBuilder().build();
    }

    public Cache<String, BehaviorSubject<Boolean>> getBookmarksCache() {
        return bookmarksCache;
    }

    public Observable<Boolean> getBookmarkForShout(@NonNull String shoutId, boolean startingValue) {
        return getWithCallableUnchecked(bookmarksCache, shoutId, () -> BehaviorSubject.create(startingValue));
    }

    public void updateBookmark(@NonNull String shoutId, boolean value) {
        getWithCallableUnchecked(bookmarksCache, shoutId, BehaviorSubject::create).onNext(value);
    }

    public void updateBookmarkMap(@NonNull Map<String, Boolean> map) {
        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            updateBookmark(entry.getKey(), entry.getValue());
        }
    }

    private static <T, R> R getWithCallableUnchecked(Cache<T, R> loadingCache, T key, Callable<R> callable) {
        try {
            return loadingCache.get(key, callable);
        } catch (ExecutionException e) {
            throw new RuntimeException("failed to load value");
        }
    }
}
