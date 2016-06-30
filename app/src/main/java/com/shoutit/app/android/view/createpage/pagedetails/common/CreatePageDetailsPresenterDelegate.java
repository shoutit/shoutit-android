package com.shoutit.app.android.view.createpage.pagedetails.common;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.PageCategory;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Scheduler;
import rx.Subscription;

public class CreatePageDetailsPresenterDelegate {

    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final String mCategoryId;

    @Inject
    public CreatePageDetailsPresenterDelegate(ApiService apiService, @NetworkScheduler Scheduler networkScheduler, @UiScheduler Scheduler uiScheduler, String categoryId) {
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mCategoryId = categoryId;
    }

    public Subscription register(CreatePageDetailsListener createPageDetailsListener) {
        createPageDetailsListener.showProgress(true);
        return mApiService.pagesCategories()
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(categories -> {
                    createPageDetailsListener.showProgress(false);
                    final PageCategory currentCategory = getCurrentCategory(categories);
                    createPageDetailsListener.setToolbarTitle(currentCategory.getName());
                    final List<CategoryInfo> categoryInfos = getCategoryInfos(currentCategory);
                    createPageDetailsListener.setCategories(categoryInfos);
                }, throwable -> {
                    createPageDetailsListener.error(throwable);
                });
    }

    private PageCategory getCurrentCategory(List<PageCategory> categories) {
        return Iterables.filter(categories, input -> {
            assert input != null;
            return input.getId().equals(mCategoryId);
        }).iterator().next();
    }

    private List<CategoryInfo> getCategoryInfos(PageCategory currentCategory) {
        return ImmutableList.copyOf(Iterables.transform(currentCategory.getChildren(),
                new Function<PageCategory, CategoryInfo>() {
                    @Nullable
                    @Override
                    public CategoryInfo apply(@Nullable PageCategory input) {
                        assert input != null;
                        return new CategoryInfo(input.getImage(), input.getName(), input.getSlug());
                    }
                }));
    }
}
