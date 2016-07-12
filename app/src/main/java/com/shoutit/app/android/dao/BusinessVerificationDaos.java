package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BusinessVerificationResponse;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.subjects.PublishSubject;

@Singleton
public class BusinessVerificationDaos {

    private final LoadingCache<String, BusinessVerificationDao> cache;
    private final ApiService apiService;
    private final Scheduler networkScheduler;

    @Inject
    public BusinessVerificationDaos(ApiService apiService,
                                   @NetworkScheduler Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, BusinessVerificationDao>() {
                    @Override
                    public BusinessVerificationDao load(@Nonnull String pageUsername) throws Exception {
                        return new BusinessVerificationDao(pageUsername);
                    }
                });
    }

    public BusinessVerificationDao getDao(@Nonnull String pageUsername) {
        return cache.getUnchecked(pageUsername);
    }

    public class BusinessVerificationDao {

        private PublishSubject<BusinessVerificationResponse> verificationResponseResultsSubject = PublishSubject.create();
        private final Observable<ResponseOrError<BusinessVerificationResponse>> verificationObservable;

        public BusinessVerificationDao(@Nonnull String pageUserName) {

            verificationObservable = apiService
                    .getBusinessVerification(pageUserName)
                    .subscribeOn(networkScheduler)
                    .mergeWith(Observable.never())
                    .mergeWith(verificationResponseResultsSubject)
                    .compose(ResponseOrError.toResponseOrErrorObservable())
                    .compose(MoreOperators.cacheWithTimeout(networkScheduler));
        }

        public Observable<ResponseOrError<BusinessVerificationResponse>> getVerificationObservable() {
            return verificationObservable;
        }

        public Observer<BusinessVerificationResponse> getVerificationResponseResultsObserver() {
            return verificationResponseResultsSubject;
        }
    }
}
