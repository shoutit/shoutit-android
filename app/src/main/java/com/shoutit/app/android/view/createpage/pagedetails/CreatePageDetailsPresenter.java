package com.shoutit.app.android.view.createpage.pagedetails;

import android.support.annotation.NonNull;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.PageCategory;
import com.shoutit.app.android.api.model.login.LoginProfile;
import com.shoutit.app.android.api.model.login.PageLoginRequest;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.LoginUtils;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;

public class CreatePageDetailsPresenter {

    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final MixPanel mMixPanel;
    private final String mCategoryId;
    private final UserPreferences mUserPreferences;
    private Listener mListener;
    private CompositeSubscription mCompositeSubscription;

    @Inject
    public CreatePageDetailsPresenter(@NonNull ApiService apiService,
                                      @NetworkScheduler Scheduler networkScheduler,
                                      @UiScheduler Scheduler uiScheduler,
                                      MixPanel mixPanel,
                                      String categoryId,
                                      UserPreferences userPreferences) {
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mMixPanel = mixPanel;
        mCategoryId = categoryId;
        mUserPreferences = userPreferences;
    }

    public void register(Listener listener) {
        mListener = listener;
        listener.showProgress(true);
        mCompositeSubscription = new CompositeSubscription(mApiService.pagesCategories()
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(categories -> {
                    listener.showProgress(false);
                    final List<CategoryInfo> categoryInfos = getCategoryInfos(categories);
                    listener.setCategories(categoryInfos);

                }, throwable -> {
                    listener.error();
                }));
    }

    private List<CategoryInfo> getCategoryInfos(List<PageCategory> categories) {
        return ImmutableList.copyOf(Iterables.transform(Iterables.filter(categories, input -> {
            assert input != null;
            return input.getId().equals(mCategoryId);
        }).iterator().next().getChildren(), new Function<PageCategory, CategoryInfo>() {
            @Nullable
            @Override
            public CategoryInfo apply(@Nullable PageCategory input) {
                assert input != null;
                return new CategoryInfo(input.getImage(), input.getName(), input.getSlug());
            }
        }));
    }

    public void unregister() {
        mListener = null;
        mCompositeSubscription.unsubscribe();
    }

    public void passCreatePageData(@NonNull CreatePageData createPageData) {
        final boolean emailEmpty = Strings.isNullOrEmpty(createPageData.mEmail);
        final boolean fullNameEmpty = Strings.isNullOrEmpty(createPageData.mFullName);
        final boolean passwordEmpty = LoginUtils.isPasswordCorrect(createPageData.mPassword);
        final boolean nameEmpty = LoginUtils.isPasswordCorrect(createPageData.mName);

        if (emailEmpty) mListener.emptyEmail();
        if (fullNameEmpty) mListener.fullNameEmpty();
        if (passwordEmpty) mListener.passwordEmpty();
        if (nameEmpty) mListener.nameEmpty();

        if (!emailEmpty && !fullNameEmpty && !passwordEmpty) {
            mListener.showProgress(true);
            mCompositeSubscription.add(mApiService.createPage(
                    new PageLoginRequest(
                            createPageData.mEmail,
                            createPageData.mPassword,
                            LoginProfile.loginUser(mUserPreferences.getLocation()),
                            mMixPanel.getDistinctId(),
                            createPageData.mItem.getSlug(),
                            createPageData.mName,
                            createPageData.mFullName))
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler)
                    .subscribe(signResponse -> {
                        mUserPreferences.setLoggedIn(signResponse.getAccessToken(),
                                signResponse.getRefreshToken(), signResponse.getProfile());
                        mUserPreferences.setGcmPushToken(null);
                        mListener.showProgress(false);
                        mListener.startMainActivity();
                    }, throwable -> {
                        mListener.error();
                    }));
        }
    }

    public interface Listener {

        void setCategories(List<CategoryInfo> categoryInfos);

        void showProgress(boolean show);

        void error();

        void emptyEmail();

        void fullNameEmpty();

        void passwordEmpty();

        void startMainActivity();

        void nameEmpty();
    }

    public static class CategoryInfo {
        private final String image;
        private final String name;
        private final String slug;

        public CategoryInfo(String image, String name, String id) {
            this.image = image;
            this.name = name;
            this.slug = id;
        }

        public String getImage() {
            return image;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
    }

    public static class CreatePageData {
        private final CategoryInfo mItem;
        private final String mName;
        private final String mFullName;
        private final String mEmail;
        private final String mPassword;

        public CreatePageData(CreatePageDetailsPresenter.CategoryInfo item, String name, String fullName, String email, String password) {
            mItem = item;
            mName = name;
            mFullName = fullName;
            mEmail = email;
            mPassword = password;
        }
    }
}