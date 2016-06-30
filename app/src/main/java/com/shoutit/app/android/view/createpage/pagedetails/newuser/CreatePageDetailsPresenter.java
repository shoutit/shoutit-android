package com.shoutit.app.android.view.createpage.pagedetails.newuser;

import android.support.annotation.NonNull;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Strings;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.login.LoginProfile;
import com.shoutit.app.android.api.model.login.PageLoginRequest;
import com.shoutit.app.android.mixpanel.MixPanel;
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
    private final MixPanel mMixPanel;
    private final UserPreferences mUserPreferences;
    private Listener mListener;
    private CompositeSubscription mCompositeSubscription;
    private final CreatePageDetailsPresenterDelegate mCreatePageDetailsPresenterDelegate;

    @Inject
    public CreatePageDetailsPresenter(@NonNull ApiService apiService,
                                      @NetworkScheduler Scheduler networkScheduler,
                                      @UiScheduler Scheduler uiScheduler,
                                      MixPanel mixPanel,
                                      UserPreferences userPreferences,
                                      CreatePageDetailsPresenterDelegate createPageDetailsPresenterDelegate) {
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mMixPanel = mixPanel;
        mUserPreferences = userPreferences;
        mCreatePageDetailsPresenterDelegate = createPageDetailsPresenterDelegate;
    }

    public void register(Listener listener) {
        mListener = listener;
        listener.showProgress(true);
        mCompositeSubscription = new CompositeSubscription(mCreatePageDetailsPresenterDelegate.register(listener));
    }

    public void unregister() {
        mListener = null;
        mCompositeSubscription.unsubscribe();
    }

    public void passCreatePageData(@NonNull CreatePageData createPageData) {
        final boolean emailEmpty = Strings.isNullOrEmpty(createPageData.mEmail);
        final boolean fullNameEmpty = Strings.isNullOrEmpty(createPageData.mFullName);
        final boolean passwordEmpty = !LoginUtils.isPasswordCorrect(createPageData.mPassword);
        final boolean nameEmpty = Strings.isNullOrEmpty(createPageData.mName);

        if (emailEmpty) mListener.emptyEmail();
        if (fullNameEmpty) mListener.fullNameEmpty();
        if (passwordEmpty) mListener.passwordEmpty();
        if (nameEmpty) mListener.nameEmpty();

        if (!emailEmpty && !fullNameEmpty && !passwordEmpty && !nameEmpty) {
            mListener.showProgress(true);
            mCompositeSubscription.add(mApiService.createPageAndLogin(
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
                        mUserPreferences.setPageLoggedIn(signResponse.getAccessToken(),
                                signResponse.getRefreshToken(), signResponse.getProfile());
                        mUserPreferences.setGcmPushToken(null);
                        mListener.showProgress(false);
                        mListener.startMainActivity();
                    }, throwable -> {
                        mListener.showProgress(false);
                        mListener.error(throwable);
                    }));
        }
    }

    public interface Listener extends CreatePageDetailsListener {

        void emptyEmail();

        void fullNameEmpty();

        void passwordEmpty();

        void nameEmpty();
    }

    public static class CreatePageData {
        private final CategoryInfo mItem;
        private final String mName;
        private final String mFullName;
        private final String mEmail;
        private final String mPassword;

        public CreatePageData(CategoryInfo item, String name, String fullName, String email, String password) {
            mItem = item;
            mName = name;
            mFullName = fullName;
            mEmail = email;
            mPassword = password;
        }
    }
}