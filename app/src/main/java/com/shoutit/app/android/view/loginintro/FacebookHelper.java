package com.shoutit.app.android.view.loginintro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.applinks.AppLinkData;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.LinkedAccounts;
import com.shoutit.app.android.api.model.UpdateFacebookTokenRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.pusher.PusherHelper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import bolts.AppLinks;
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

    public static final String FACEBOOK_SHARE_APP_LINK = "https://fb.me/1224908360855680";

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
        FacebookSdk.sdkInitialize(context, this::refreshTokenIfNeeded);
    }

    public static void checkForAppLinks(@Nonnull Context context, @Nonnull Intent intent) {
        LogHelper.logIfDebug(TAG, "Checking for App Links");
        final Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(context, intent);

        if (targetUrl != null) {
            LogHelper.logIfDebug(TAG, "Target url != null");
            context.startActivity(new Intent(Intent.ACTION_VIEW, targetUrl));
        } else {
            AppLinkData.fetchDeferredAppLinkData(context, FacebookHelper::processAppLinkData);
        }
    }

    private static void processAppLinkData(@Nullable AppLinkData appLinkData) {
        LogHelper.logIfDebug(TAG, "processing App Link data");
        if (appLinkData == null) {
            return;
        }

        if ("InviteFriends".equals(appLinkData.getPromotionCode())) {
            LogHelper.logIfDebug(TAG, "ref data" + appLinkData.getRef() + " reference: " + appLinkData.getRefererData().toString());
        }
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
                        .subscribe(responseBody -> {
                            stopTracking();
                            LogHelper.logIfDebug(TAG, "Facebook token updated");
                        }, throwable -> {
                            stopTracking();
                            LogHelper.logThrowableAndCrashlytics(TAG, "Cannot update fb token", throwable);
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
                                    LogHelper.logIfDebug(TAG, "onCancel while asking for permission");
                                    if (!subscriber.isUnsubscribed()) {
                                        subscriber.onNext(false);
                                    }
                                }

                                @Override
                                public void onError(FacebookException error) {
                                    LogHelper.logIfDebug(TAG, "Error while asking for permission: " + error.getMessage());
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
        return responseBody -> {
            LogHelper.logIfDebug(TAG, "Waiting for user to be updated in API");
            return pusherHelper.getUserUpdatedObservable()
                    .filter(user -> {
                        userPreferences.updateUserJson(user);
                        LogHelper.logIfDebug(TAG, "Pusher event: User updated in API");

                        return hasRequiredPermissionInApi(user, requiredPermissionName);
                    })
                    .map(user -> ResponseOrError.fromData(true));
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

    public static void showAppInviteDialog(@Nonnull Activity activity,
                                           @Nonnull String appLinkUrl,
                                           @Nonnull CallbackManager callbackManager,
                                           @Nullable String promotionalCode) {
        if (AppInviteDialog.canShow()) {
            final AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
                    .setPromotionDetails("InviteFriendsPromoCode",  promotionalCode)
                    .build();

            AppInviteDialog appInviteDialog = new AppInviteDialog(activity);
            appInviteDialog.registerCallback(callbackManager, new FacebookCallback<AppInviteDialog.Result>() {
                @Override
                public void onSuccess(AppInviteDialog.Result result) {
                    LogHelper.logIfDebug(TAG, "Successful app invite");
                }

                @Override
                public void onCancel() {
                    LogHelper.logIfDebug(TAG, "App invite canceled");
                }

                @Override
                public void onError(FacebookException error) {
                    LogHelper.logIfDebug(TAG, "Failed to app invite");
                    ColoredSnackBar.error(ColoredSnackBar.contentView(activity),
                            R.string.invite_error, Snackbar.LENGTH_LONG).show();
                }
            });

            appInviteDialog.show(content);
        }
    }
}
