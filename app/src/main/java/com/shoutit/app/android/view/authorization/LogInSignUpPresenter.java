package com.shoutit.app.android.view.authorization;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Strings;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;
import com.shoutit.app.android.api.model.login.LoginProfile;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.Validators;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Scheduler;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class LogInSignUpPresenter {

    public interface LogInDelegate {
        void showProgress(boolean show);
        void showEmailError(String message);
        void showPasswordError(String message);
        void showApiError(Throwable throwable);
        void showSignUpScreen();
        void showHomeScreen();
    }

    private static final int MIN_PASSWORD_CHARS = 6;
    private static final int MAX_PASSWORD_CHARS = 20;

    @NonNull
    private final ApiService apiService;
    @NonNull
    private final UserPreferences userPreferences;
    @NonNull
    private final Scheduler networkScheduler;
    @NonNull
    private final Scheduler uiScheduler;
    @Nonnull
    private final MixPanel mixPanel;
    @Nonnull
    private final Resources resources;
    private LogInDelegate delegate;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public LogInSignUpPresenter(@NonNull final ApiService apiService,
                                @NonNull final UserPreferences userPreferences,
                                @NonNull @NetworkScheduler final Scheduler networkScheduler,
                                @NonNull @UiScheduler final Scheduler uiScheduler,
                                @Nonnull final MixPanel mixPanel,
                                @Nonnull @ForActivity Resources resources) {
        this.apiService = apiService;
        this.userPreferences = userPreferences;
        this.networkScheduler = networkScheduler;
        this.uiScheduler = uiScheduler;
        this.mixPanel = mixPanel;
        this.resources = resources;
    }

    public void register(LogInDelegate delegate) {
        this.delegate = delegate;
    }

    public void unregister() {
        subscriptions.unsubscribe();
    }

    public void login(String email, String password) {
        if (!areDataValid(email, password)) {
            return;
        }

        makeLoginRequest(email, password);
    }

    private void makeLoginRequest(@Nonnull String email, @Nonnull String password) {
        final UserLocation location = userPreferences.getLocation();

        delegate.showProgress(true);

        final Subscription login = apiService.login(new EmailLoginRequest(
                email, password, LoginProfile.loginUser(location), mixPanel.getDistinctId()))
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .subscribe(signResponse -> {
                    delegate.showProgress(false);

                    if (signResponse.isNewSignup()) {
                        delegate.showSignUpScreen();
                    } else {
                        delegate.showHomeScreen();
                    }
                }, throwable -> {
                    delegate.showProgress(false);
                    delegate.showApiError(throwable);
                });
        subscriptions.add(login);
    }

    private boolean areDataValid(@Nullable String email, @Nullable String password) {
        boolean isEmailValid = Validators.isEmailValid(email);
        boolean isPasswordValid =  !Strings.isNullOrEmpty(password) &&
                password.length() >= MIN_PASSWORD_CHARS && password.length() <= MAX_PASSWORD_CHARS;

        delegate.showEmailError(isEmailValid ? null : resources.getString(R.string.register_empty_mail));
        delegate.showPasswordError(isPasswordValid ? null : resources.getString(R.string.register_empty_password));

        return isEmailValid && isPasswordValid;
    }
}
