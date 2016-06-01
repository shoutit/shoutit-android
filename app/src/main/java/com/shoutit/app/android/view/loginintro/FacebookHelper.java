package com.shoutit.app.android.view.loginintro;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.LinkedAccounts;
import com.shoutit.app.android.api.model.UpdateFacebookTokenRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.pusher.PusherHelper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

public class FacebookHelper {

    private static final String TAG = FacebookHelper.class.getSimpleName();

    private static final String PERMISSION_EMAIL = "email";
    public static final String PERMISSION_USER_FRIENDS = "user_friends";
    public static final String PERMISSION_PUBLISH_ACTIONS = "publish_actions";

    private final ApiService apiService;
    private final UserPreferences userPreferences;
    private final Context context;
    private final Scheduler networkScheduler;
    private final PusherHelper pusherHelper;

    public FacebookHelper(final ApiService apiService,
                          final UserPreferences userPreferences,
                          @ForApplication Context context,
                          @NetworkScheduler final Scheduler networkScheduler,
                          PusherHelper pusherHelper) {
        this.apiService = apiService;
        this.userPreferences = userPreferences;
        this.context = context;
        this.networkScheduler = networkScheduler;
        this.pusherHelper = pusherHelper;
    }

    public void initFacebook() {
        FacebookSdk.sdkInitialize(context, new FacebookSdk.InitializeCallback() {
            @Override
            public void onInitialized() {
                refreshTokenIfNeeded();
            }
        });
    }

    private void refreshTokenIfNeeded() {
        final User currentUser = userPreferences.getUser();
        if (currentUser == null || !shouldRefreshToken(currentUser)) {
            return;
        }

        new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (!userPreferences.isNormalUser()) {
                    return;
                }

                updateFacebookTokenInApi(currentAccessToken.getToken())
                        .subscribe(new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody responseBody) {
                                stopTracking();
                                LogHelper.logIfDebug(TAG, "Facebook token updated");
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                stopTracking();
                                LogHelper.logThrowableAndCrashlytics(TAG, "Cannot update fb token", throwable);
                            }
                        });
            }
        };

        LogHelper.logIfDebug(TAG, "Facebook token expired");
        AccessToken.refreshCurrentAccessTokenAsync();
    }

    public Observable<ResponseBody> updateFacebookTokenInApi(@Nonnull String facebookToken) {
        return apiService.updateFacebookToken(new UpdateFacebookTokenRequest(facebookToken))
                .subscribeOn(networkScheduler);
    }

    public static void logOutFromFacebook() {
        LoginManager.getInstance().logOut();
    }

    /**
     * @param activity
     * @param permissionName
     * @return true if permission granted, false otherwise
     */
    public Observable<ResponseOrError<Boolean>> askForPermissionIfNeeded(@Nonnull final Activity activity,
                                                                         @Nonnull final String permissionName,
                                                                         @Nonnull final CallbackManager callbackManager,
                                                                         final boolean isPublishPermission) {
        final User user = userPreferences.getUser();

        if (user != null && hasRequiredPermissionInApi(user, permissionName)) {
            return Observable.just(ResponseOrError.fromData(true));
        } else {
            return Observable
                    .create(new Observable.OnSubscribe<Boolean>() {
                        @Override
                        public void call(final Subscriber<? super Boolean> subscriber) {
                            final LoginManager loginManager = LoginManager.getInstance();

                            if (!isLoggedInToFacebook()) {
                                LogHelper.logIfDebug(TAG, "Not logged in to facebook");

                                final List<String> permissions = Lists.newArrayList();
                                permissions.add(PERMISSION_EMAIL);

                                if (!isPublishPermission) {
                                    permissions.add(permissionName);
                                }

                                loginManager.logInWithReadPermissions(activity, permissions);

                            } else if (!isPublishPermission) {
                                loginManager.logInWithReadPermissions(activity, Collections.singleton(permissionName));
                            }

                            if (isPublishPermission) {
                                loginManager.logInWithPublishPermissions(activity, Collections.singleton(permissionName));
                            }

                            loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                                @Override
                                public void onSuccess(LoginResult loginResult) {
                                    if (isPermissionGranted(permissionName) && !subscriber.isUnsubscribed()) {
                                        LogHelper.logIfDebug(TAG, "Persmission " + permissionName + " has been granted");
                                        subscriber.onNext(true);
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
                                    if (error instanceof FacebookAuthorizationException &&
                                            AccessToken.getCurrentAccessToken() != null) {
                                        // Case when app has token from other user and tries to log in
                                        // as different one.
                                        loginManager.logOut();
                                    }

                                    if (!subscriber.isUnsubscribed()) {
                                        subscriber.onError(error);
                                    }
                                }
                            });
                        }
                    })
                    .compose(ResponseOrError.<Boolean>toResponseOrErrorObservable())
                    .compose(ResponseOrError.switchMap(new Func1<Boolean, Observable<ResponseOrError<Boolean>>>() {
                        @Override
                        public Observable<ResponseOrError<Boolean>> call(Boolean isPermissionGranted) {
                            if (isPermissionGranted) {
                                return updateFacebookTokenInApi(AccessToken.getCurrentAccessToken().getToken())
                                        .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable())
                                        .compose(ResponseOrError.switchMap(waitForUserToBeUpdatedInApi(permissionName)));
                            } else {
                                return Observable.just(ResponseOrError.fromData(false));
                            }
                        }
                    }));
        }
    }

    @NonNull
    private Func1<ResponseBody, Observable<ResponseOrError<Boolean>>> waitForUserToBeUpdatedInApi(@Nonnull final String requiredPermissionName) {
        return new Func1<ResponseBody, Observable<ResponseOrError<Boolean>>>() {
            @Override
            public Observable<ResponseOrError<Boolean>> call(ResponseBody responseBody) {
                LogHelper.logIfDebug(TAG, "Waiting for user to be updated in API");
                return pusherHelper.getUserUpdatedObservable()
                        .filter(new Func1<User, Boolean>() {
                            @Override
                            public Boolean call(User user) {
                                userPreferences.updateUserJson(user);
                                LogHelper.logIfDebug(TAG, "Pusher event: User updated in API");
                                
                                return hasRequiredPermissionInApi(user, requiredPermissionName);
                            }
                        })
                        .map(new Func1<User, ResponseOrError<Boolean>>() {
                            @Override
                            public ResponseOrError<Boolean> call(User user) {
                                return ResponseOrError.fromData(true);
                            }
                        });
            }
        };
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

    public boolean hasRequiredPermissionInApi(@Nonnull User user, @Nonnull String permissionName) {
        if (user.getLinkedAccounts() != null &&
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
