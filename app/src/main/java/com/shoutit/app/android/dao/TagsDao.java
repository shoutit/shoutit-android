package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.TagDetail;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class TagsDao {

    @Nonnull
    private final LoadingCache<String, TagDao> tagsCache;
    @Nonnull
    private final ApiService apiService;
    private final Scheduler networkScheduler;

    public TagsDao(@Nonnull ApiService apiService,
                       @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        tagsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, TagDao>() {
                    @Override
                    public TagDao load(@Nonnull String tagName) throws Exception {
                        return new TagDao(tagName);
                    }
                });
    }

    @Nonnull
    public Observable<ResponseOrError<TagDetail>> getTagObservable(@Nonnull String tagName) {
        return tagsCache.getUnchecked(tagName).getTagObservable();
    }

    @Nonnull
    public Observer<ResponseOrError<TagDetail>> getUpdatedTagObserver(@Nonnull String tagName) {
        return tagsCache.getUnchecked(tagName).updatedTagObserver();
    }

    public class TagDao {
        @Nonnull
        private Observable<ResponseOrError<TagDetail>> tagObservable;
        @Nonnull
        private PublishSubject<ResponseOrError<TagDetail>> updatedTagSubject = PublishSubject.create();

        public TagDao(@Nonnull final String tagName) {
            tagObservable = apiService.tagDetail(tagName)
                    .subscribeOn(networkScheduler)
                    .compose(ResponseOrError.<TagDetail>toResponseOrErrorObservable())
                    .mergeWith(updatedTagSubject)
                    .compose(MoreOperators.<ResponseOrError<TagDetail>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<TagDetail>> getTagObservable() {
            return tagObservable;
        }

        @Nonnull
        public Observer<ResponseOrError<TagDetail>> updatedTagObserver() {
            return updatedTagSubject;
        }
    }
}

