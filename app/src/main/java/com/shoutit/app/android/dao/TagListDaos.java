package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.TagsListResponse;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class TagListDaos {

    private final LoadingCache<String, TagListDao> tagListCache;
    private final ApiService apiService;
    private final Scheduler networkScheduler;

    public TagListDaos(ApiService apiService,
                       @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        tagListCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, TagListDao>() {
                    @Override
                    public TagListDao load(@Nonnull String userName) throws Exception {
                        return new TagListDao(userName);
                    }
                });
    }

    @Nonnull
    public TagListDao getDao(String userName) {
        return tagListCache.getUnchecked(userName);
    }


    public class TagListDao {

        @Nonnull
        private final Observable<ResponseOrError<TagsListResponse>> tagsObservable;
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<ResponseOrError<TagsListResponse>> updatedTagLocallySubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();

        public TagListDao(String userName) {
            final OperatorMergeNextToken<TagsListResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<TagsListResponse, Observable<TagsListResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<TagsListResponse> call(TagsListResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                if (previousResponse == null) {
                                    pageNumber = 0;
                                }
                                ++pageNumber;

                                final Observable<TagsListResponse> apiRequest = apiService.tagsListenings(userName, pageNumber, 20)
                                        .subscribeOn(networkScheduler);

                                if (previousResponse == null) {
                                    return apiRequest;
                                } else {
                                    return Observable.just(previousResponse).zipWith(apiRequest, new MergeTagsListResponses());
                                }
                            } else {
                                return Observable.never();
                            }
                        }
                    });


            tagsObservable = loadMoreSubject.startWith((Object) null)
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<TagsListResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<TagsListResponse>>refresh(refreshSubject))
                    .mergeWith(updatedTagLocallySubject)
                    .compose(MoreOperators.<ResponseOrError<TagsListResponse>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<TagsListResponse>> getTagsObservable() {
            return tagsObservable;
        }

        @Nonnull
        public PublishSubject<ResponseOrError<TagsListResponse>> getUpdatedTagLocallySubject() {
            return updatedTagLocallySubject;
        }

        @Nonnull
        public PublishSubject<Object> getLoadMoreSubject() {
            return loadMoreSubject;
        }

        @Nonnull
        public PublishSubject<Object> getRefreshSubject() {
            return refreshSubject;
        }
    }

    public class MergeTagsListResponses implements Func2<TagsListResponse, TagsListResponse, TagsListResponse> {
        @Override
        public TagsListResponse call(TagsListResponse previousData, TagsListResponse newData) {
            final ImmutableList<TagDetail> allItems = ImmutableList.<TagDetail>builder()
                    .addAll(previousData.getResults())
                    .addAll(newData.getResults())
                    .build();

            return new TagsListResponse(newData.getCount(), newData.getNext(), newData.getPrevious(), allItems);
        }
    }
}
