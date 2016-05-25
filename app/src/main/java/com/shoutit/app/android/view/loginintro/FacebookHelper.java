package com.shoutit.app.android.view.loginintro;

import android.app.Activity;
import android.content.Context;

import com.appunite.rx.dagger.NetworkScheduler;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.LinkedAccounts;
import com.shoutit.app.android.api.model.UpdateFacebookTokenRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.LogHelper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

@Singleton
public class FacebookHelper {

    private static final String TAG = FacebookHelper.class.getSimpleName();

    private static final String PERMISSION_EMAIL = "email";
    private static final String PERMISSION_PUBLISH_ACTIONS = "publish_actions";

    private final ApiService apiService;
    private final UserPreferences userPreferences;
    private final Context context;
    private final Scheduler networkScheduler;

    @Inject
    public FacebookHelper(final ApiService apiService,
                          final UserPreferences userPreferences,
                          @ForApplication Context context,
                          @NetworkScheduler final Scheduler networkScheduler) {
        this.apiService = apiService;
        this.userPreferences = userPreferences;
        this.context = context;
        this.networkScheduler = networkScheduler;
    }

    public void initFacebook() {
        FacebookSdk.sdkInitialize(context, new FacebookSdk.InitializeCallback() {
            @Override
            public void onInitialized() {

                new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                               AccessToken currentAccessToken) {
                        if (!userPreferences.isNormalUser()) {
                            return;
                        }

                        updateFacebookTokenInApi(currentAccessToken.getToken())
                                .subscribe(new Action1<User>() {
                                    @Override
                                    public void call(User user) {
                                        userPreferences.updateUserJson(user);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        LogHelper.logThrowableAndCrashlytics(TAG, "Cannot update fb token", throwable);
                                    }
                                });
                    }
                };

                refreshTokenIfNeeded();
            }
        });
    }

    public Observable<User> updateFacebookTokenInApi(@Nonnull String facebookToken) {
        return apiService.updateFacebookToken(new UpdateFacebookTokenRequest(facebookToken))
                .subscribeOn(networkScheduler);

    }

    public void refreshTokenIfNeeded() {
        final User currentUser = userPreferences.getUser();
        if (currentUser == null || !shouldRefreshToken(currentUser)) {
            return;
        }

        AccessToken.refreshCurrentAccessTokenAsync();
    }

    /**
     * @param activity
     * @param permissionName
     * @return true if already has permission, false otherwise
     */
    public Observable<Boolean> askForPublicPermissionIfNeeded(@Nonnull final Activity activity,
                                                  @Nonnull final String permissionName,
                                                  @Nonnull final CallbackManager callbackManager) {
        if (hasRequiredPermissionInApi(permissionName)) {
            return Observable.just(true);
        } else {
            return Observable
                    .create(new Observable.OnSubscribe<Boolean>() {
                        @Override
                        public void call(final Subscriber<? super Boolean> subscriber) {
                            final LoginManager loginManager = LoginManager.getInstance();
                            if (!isLoggedInToFacebook()) {
                                loginManager.logInWithReadPermissions(activity, Collections.singleton(PERMISSION_EMAIL));
                            }

                            loginManager.logInWithPublishPermissions(activity, Collections.singleton(permissionName));

                            loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                                @Override
                                public void onSuccess(LoginResult loginResult) {
                                    if (isPermissionGranted(permissionName)) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onNext(true);
                                        }
                                    }
                                }

                                @Override
                                public void onCancel() {
                                    if (!subscriber.isUnsubscribed()) {
                                        subscriber.onNext(false);
                                    }
                                }

                                @Override
                                public void onError(FacebookException error) {
                                    if (!subscriber.isUnsubscribed()) {
                                        subscriber.onError(error);
                                    }
                                }
                            });
                        }
                    })
                    .switchMap(new Func1<Boolean, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(Boolean isPermissionGranted) {
                            if (isPermissionGranted) {
                                return updateFacebookTokenInApi(AccessToken.getCurrentAccessToken().getToken())
                                        .map(new Func1<User, Boolean>() {
                                            @Override
                                            public Boolean call(User user) {
                                                userPreferences.updateUserJson(user);
                                                return hasRequiredPermissionInApi(permissionName);
                                            }
                                        });
                            } else {
                                return Observable.just(false);
                            }
                        }
                    });
        }
    }

    private boolean isLoggedInToFacebook() {
        final User user = userPreferences.getUser();
        return user != null &&
                user.getLinkedAccounts() != null &&
                user.getLinkedAccounts().getFacebook() != null;
    }

    private boolean isPermissionGranted(@Nonnull String permissionName) {
        final Set<String> permissions = AccessToken.getCurrentAccessToken().getPermissions();
        return permissions.contains(permissionName);
    }

    public boolean hasRequiredPermissionInApi(@Nonnull String permissionName) {
        final User user = userPreferences.getUser();

        if (user != null && user.getLinkedAccounts() != null &&
                user.getLinkedAccounts().getFacebook() != null) {
            final List<String> scopes = user.getLinkedAccounts().getFacebook().getScopes();
            for (String scope : scopes) {
                if (scope.equals(permissionName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldRefreshToken(@Nonnull User user) {
        final long currentTimeInSecond = System.currentTimeMillis() / 1000;
        final LinkedAccounts linkedAccounts = user.getLinkedAccounts();

        return linkedAccounts != null && linkedAccounts.getFacebook() != null &&
                currentTimeInSecond >= linkedAccounts.getFacebook().getExpiresAt();
    }

    public static Observable<String> getToken(final Activity activity, final CallbackManager callbackManager) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                final LoginManager instance = LoginManager.getInstance();

                instance.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        if (!subscriber.isUnsubscribed()) {
                            final AccessToken accessToken = loginResult.getAccessToken();
                            subscriber.onNext(accessToken.getToken());
                            subscriber.onCompleted();
                        }
                    }

                    @Override
                    public void onCancel() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }

                    @Override
                    public void onError(FacebookException error) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(error);
                        }
                    }
                });

                instance.logInWithReadPermissions(activity, Collections.singletonList(PERMISSION_EMAIL));
            }
        });
    }
}
