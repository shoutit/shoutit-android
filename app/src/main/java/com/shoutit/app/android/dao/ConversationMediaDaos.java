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
import com.shoutit.app.android.api.model.ConversationMediaResponse;
import com.shoutit.app.android.api.model.MessageAttachment;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ConversationMediaDaos {

    private static final Integer PAGE_SIZE = 20;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;

    private final LoadingCache<String, ConversationMediaDao> mediaCache;

    public ConversationMediaDaos(@Nonnull ApiService apiService,
                                 @Nonnull @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        mediaCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ConversationMediaDao>() {
                    @Override
                    public ConversationMediaDao load(@Nonnull String conversationId) throws Exception {
                        return new ConversationMediaDao(conversationId);
                    }
                });
    }

    public ConversationMediaDao getDao(@Nonnull String conversationId) {
        return mediaCache.getUnchecked(conversationId);
    }

    public class ConversationMediaDao {

        @Nonnull
        private final Observable<ResponseOrError<ConversationMediaResponse>> mediaObservable;
        @Nonnull
        private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();

        public ConversationMediaDao(@Nonnull final String conversationId) {
            final OperatorMergeNextToken<ConversationMediaResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<ConversationMediaResponse, Observable<ConversationMediaResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<ConversationMediaResponse> call(ConversationMediaResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                if (previousResponse == null) {
                                    pageNumber = 0;
                                }
                                ++pageNumber;

                                final Observable<ConversationMediaResponse> apiRequest = apiService
                                        .conversationMedia(conversationId, pageNumber, PAGE_SIZE)
                                        .subscribeOn(networkScheduler);

                                if (previousResponse == null) {
                                    return apiRequest;
                                } else {
                                    return Observable.just(previousResponse).zipWith(apiRequest, new MergeConversationMediaResponses());
                                }
                            } else {
                                return Observable.never();
                            }
                        }
                    });

            mediaObservable = loadMoreSubject.startWith((Object) null)
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<ConversationMediaResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<ConversationMediaResponse>>cacheWithTimeout(networkScheduler))
                    .mergeWith(Observable.<ResponseOrError<ConversationMediaResponse>>never());
        }

        @Nonnull
        public Observable<ResponseOrError<ConversationMediaResponse>> getMediaObservable() {
            return mediaObservable;
        }

        @Nonnull
        public Observer<Object> getLoadMoreObserver() {
            return loadMoreSubject;
        }
    }

    private class MergeConversationMediaResponses implements rx.functions.Func2<ConversationMediaResponse, ConversationMediaResponse, ConversationMediaResponse> {
        @Override
        public ConversationMediaResponse call(ConversationMediaResponse previousResponses,
                                              ConversationMediaResponse newResponse) {
            final List<MessageAttachment> results = previousResponses.getResults();

            final ImmutableList<MessageAttachment> newResults = ImmutableList.<MessageAttachment>builder()
                    .addAll(results)
                    .addAll(newResponse.getResults())
                    .build();

            return new ConversationMediaResponse(newResponse.getCount(), newResponse.getNext(),
                    newResponse.getPrevious(), newResults);
        }
    }
}
