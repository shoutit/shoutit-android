package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.RelatedTagsResponse;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class TagsDao {

    @Nonnull
    private final LoadingCache<String, TagDao> tagsCache;
    @Nonnull
    private final LoadingCache<String, RelatedTagsDao> relatedTagsCache;
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
                    public TagDao load(@Nonnull String tagSlug) throws Exception {
                        return new TagDao(tagSlug);
                    }
                });

        relatedTagsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, RelatedTagsDao>() {
                    @Override
                    public RelatedTagsDao load(@Nonnull String tagName) throws Exception {
                        return new RelatedTagsDao(tagName);
                    }
                });
    }

    @Nonnull
    public Observable<ResponseOrError<TagDetail>> getTagObservable(@Nonnull String tagSlug) {
        return tagsCache.getUnchecked(tagSlug).getTagObservable();
    }

    @Nonnull
    public Observer<ResponseOrError<TagDetail>> getUpdatedTagObserver(@Nonnull String tagName) {
        return tagsCache.getUnchecked(tagName).updatedTagObserver();
    }

    @Nonnull
    public Observable<ResponseOrError<RelatedTagsResponse>> getRelatedTagsObservable(@Nonnull String tagName) {
        return relatedTagsCache.getUnchecked(tagName).getRelatedTagsObservable();
    }

    @Nonnull
    public Observer<ResponseOrError<RelatedTagsResponse>> getUpdatedRelatedTagsObserver(@Nonnull String tagName) {
        return relatedTagsCache.getUnchecked(tagName).updatedTagObserver();
    }

    public void refreshRelatedTags(@Nonnull String tagName) {
        relatedTagsCache.getUnchecked(tagName).getRefreshObserver().onNext(null);
    }

    public void refreshTag(@Nonnull String tagName) {
        tagsCache.getUnchecked(tagName).getRefreshObserver().onNext(null);
    }

    public class TagDao {
        @Nonnull
        private Observable<ResponseOrError<TagDetail>> tagObservable;
        @Nonnull
        private PublishSubject<ResponseOrError<TagDetail>> updatedTagSubject = PublishSubject.create();
        @Nonnull
        private PublishSubject<Object> refreshSubject = PublishSubject.create();

        public TagDao(@Nonnull final String tagSlug) {
            tagObservable = apiService.tagDetail(tagSlug)
                    .subscribeOn(networkScheduler)
                    .compose(MoreOperators.<TagDetail>refresh(refreshSubject))
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

        @Nonnull
        public Observer<Object> getRefreshObserver() {
            return refreshSubject;
        }
    }

    public class RelatedTagsDao {
        @Nonnull
        private Observable<ResponseOrError<RelatedTagsResponse>> tagsObservable;
        @Nonnull
        private PublishSubject<ResponseOrError<RelatedTagsResponse>> updatedTagsSubject = PublishSubject.create();
        @Nonnull
        private PublishSubject<Object> refreshSubject = PublishSubject.create();

        public RelatedTagsDao(@Nonnull final String slug) {
            tagsObservable = apiService.relatedTags(slug)
                    .subscribeOn(networkScheduler)
                    .compose(MoreOperators.<RelatedTagsResponse>refresh(refreshSubject))
                    .compose(ResponseOrError.<RelatedTagsResponse>toResponseOrErrorObservable())
                    .mergeWith(updatedTagsSubject)
                    .compose(MoreOperators.<ResponseOrError<RelatedTagsResponse>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<RelatedTagsResponse>> getRelatedTagsObservable() {
            return tagsObservable;
        }

        @Nonnull
        public Observer<ResponseOrError<RelatedTagsResponse>> updatedTagObserver() {
            return updatedTagsSubject;
        }

        @Nonnull
        public Observer<Object> getRefreshObserver() {
            return refreshSubject;
        }
    }

}

