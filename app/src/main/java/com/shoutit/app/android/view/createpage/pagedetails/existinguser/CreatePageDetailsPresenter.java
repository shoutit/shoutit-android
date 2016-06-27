package com.shoutit.app.android.view.createpage.pagedetails.existinguser;

import android.support.annotation.NonNull;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Strings;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CreatePageRequest;
import com.shoutit.app.android.utils.LoginUtils;
import com.shoutit.app.android.view.createpage.pagedetails.common.CategoryInfo;
import com.shoutit.app.android.view.createpage.pagedetails.common.CreatePageDetailsListener;
import com.shoutit.app.android.view.createpage.pagedetails.common.CreatePageDetailsPresenterDelegate;

import javax.inject.Inject;

import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;

public class CreatePageDetailsPresenter {

    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private CreatePageDetailsListener mListener;
    private CompositeSubscription mCompositeSubscription;
    private final CreatePageDetailsPresenterDelegate mCreatePageDetailsPresenterDelegate;

    @Inject
    public CreatePageDetailsPresenter(@NonNull ApiService apiService,
                                      @NetworkScheduler Scheduler networkScheduler,
                                      @UiScheduler Scheduler uiScheduler,
                                      CreatePageDetailsPresenterDelegate createPageDetailsPresenterDelegate) {
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mCreatePageDetailsPresenterDelegate = createPageDetailsPresenterDelegate;
    }

    public void register(CreatePageDetailsListener listener) {
        mListener = listener;
        mCompositeSubscription = new CompositeSubscription(mCreatePageDetailsPresenterDelegate.register(listener));
    }


    public void unregister() {
        mCompositeSubscription.unsubscribe();
        mListener = null;
    }

    public void passCreatePageData(@NonNull CreatePageData createPageData) {
        final boolean nameEmpty = Strings.isNullOrEmpty(createPageData.mName);

        if (nameEmpty) mListener.nameEmpty();

        if (!nameEmpty) {
            mListener.showProgress(true);
            mCompositeSubscription.add(mApiService.createPage(
                    new CreatePageRequest(
                            createPageData.mName,
                            createPageData.mItem.getSlug()
                    ))
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler)
                    .subscribe(signResponse -> {
                        mListener.showProgress(false);
                        mListener.startMainActivity();
                    }, throwable -> {
                        mListener.error();
                    }));
        }
    }

    public static class CreatePageData {
        private final CategoryInfo mItem;
        private final String mName;

        public CreatePageData(CategoryInfo item, String name) {
            mItem = item;
            mName = name;
        }
    }
}