package com.shoutit.app.android.facebook;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.facebook.applinks.AppLinkData;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.FbAdAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.LinkedAccounts;
import com.shoutit.app.android.api.model.UpdateFacebookTokenRequest;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.pusher.PusherHelperHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.FuncN;

public class FacebookHelper {

    private static final String TAG = FacebookHelper.class.getSimpleName();

    private static final int ADS_NUM_TO_PREFETCH = 8;
    private static final int ADS_NUM_SHOUT_DETAIL_TO_PREFETCH = 5;

    private static final String PERMISSION_EMAIL = "email";
    public static final String PERMISSION_USER_FRIENDS = "user_friends";
    public static final String PERMISSION_PUBLISH_ACTIONS = "publish_actions";
    public static final String PERMISSION_MANAGE_PAGES = "manage_pages";
    public static final String PERMISSION_PUBLISH_PAGES = "publish_pages";

    public static final String[] PAGES_PERMISSIONS = new String[]{
            PERMISSION_MANAGE_PAGES, PERMISSION_PUBLISH_PAGES};

    public static final String FACEBOOK_SHARE_APP_LINK = "https://fb.me/1224908360855680";

    private final ApiService apiService;
    private final UserPreferences userPreferences;
    private final Context context;
    private final Scheduler networkScheduler;
    private final Gson gson;
    private final PusherHelperHolder pusherHelperHolder;
    private final NativeAdsManager listAdManager;
    private final NativeAdsManager shoutDetailAdManager;

    public FacebookHelper(final ApiService apiService,
                          final UserPreferences userPreferences,
                          @ForApplication Context context,
                          @NetworkScheduler final Scheduler networkScheduler,
                          PusherHelperHolder pusherHelperHolder,
                          Gson gson) {
        this.apiService = apiService;
        this.userPreferences = userPreferences;
        this.context = context;
        this.networkScheduler = networkScheduler;
        this.gson = gson;
        this.pusherHelperHolder = pusherHelperHolder;

        listAdManager = new NativeAdsManager(
                context, getListAdId(), ADS_NUM_TO_PREFETCH);

        shoutDetailAdManager = new NativeAdsManager(
                context, getShoutDetailAdId(), ADS_NUM_SHOUT_DETAIL_TO_PREFETCH);

        final NativeAdsManager.Listener adsListener = new NativeAdsManager.Listener() {
            @Override
            public void onAdsLoaded() {
                LogHelper.logIfDebug(TAG, "onAdsLoaded");
            }

            @Override
            public void onAdError(AdError adError) {
                LogHelper.logThrowableAndCrashlytics(TAG,
                        "Error while loading FB ad - onAdsError",
                        new Throwable(adError.getErrorMessage()));
            }
        };

        listAdManager.setListener(adsListener);
        shoutDetailAdManager.setListener(adsListener);

        loadAds();
    }

    public void initFacebook() {
        FacebookSdk.sdkInitialize(context, this::refreshTokenIfNeeded);
    }

    private void loadAds() {
        AdSettings.addTestDevice("e206dc0d711d548562ff65e91961aebf");
        AdSettings.addTestDevice("2527377803eebdb0123194b820f02a5a");
        AdSettings.addTestDevice("e5c7606e893f5e4e19ce8f03429c8b47");
        listAdManager.loadAds(NativeAd.MediaCacheFlag.ALL);
        shoutDetailAdManager.loadAds(NativeAd.MediaCacheFlag.ALL);
    }

    @Nonnull
    public static Observable<String> getPromotionalCodeObservable(@Nonnull Context context) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                AppLinkData.fetchDeferredAppLinkData(context, appLinkData -> {
                    if (!subscriber.isUnsubscribed()) {
                        if (appLinkData == null || appLinkData.getPromotionCode() == null) {
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                        } else {
                            LogHelper.logIfDebug(TAG, "Received promotionCode: " + appLinkData.getPromotionCode());
                            subscriber.onNext(appLinkData.getPromotionCode());
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }

    private void refreshTokenIfNeeded() {
        final BaseProfile currentUser = userPreferences.getUserOrPage();
        if (currentUser == null || !shouldRefreshToken(currentUser)) {
            return;
        }

        LogHelper.logIfDebug(TAG, "Facebook token expired");

        AccessToken.refreshCurrentAccessTokenAsync(new AccessToken.AccessTokenRefreshCallback() {
            @Override
            public void OnTokenRefreshed(AccessToken accessToken) {
                if (accessToken == null) {
                    return;
                }

                updateFacebookTokenInApi(accessToken.getToken())
                        .subscribe(responseBody -> {
                            LogHelper.logIfDebug(TAG, "Facebook token updated");
                        }, throwable -> {
                            LogHelper.logThrowableAndCrashlytics(TAG, "Cannot update fb token", throwable);
                        });
            }

            @Override
            public void OnTokenRefreshFailed(FacebookException exception) {
                LogHelper.logThrowableAndCrashlytics(TAG, "Cannot update fb token", exception);
            }
        });
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
     * @param permissions
     * @return true if permission granted, false otherwise
     */
    public Observable<ResponseOrError<Boolean>> askForPermissionIfNeeded(@Nonnull final Activity activity,
                                                                         @Nonnull final String[] permissions,
                                                                         @Nonnull final CallbackManager callbackManager,
                                                                         final boolean isPublishPermission) {
        final BaseProfile profile = userPreferences.getUserOrPage();

        if (profile != null && hasRequiredPermissions(profile, permissions)) {
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
                                    for (String permission : permissions) {
                                        permissions.add(permission);
                                    }
                                }

                                loginManager.logInWithReadPermissions(activity, permissions);

                            } else if (!isPublishPermission) {
                                loginManager.logInWithReadPermissions(activity, Arrays.asList(permissions));
                            }

                            if (isPublishPermission) {
                                loginManager.logInWithPublishPermissions(activity, Arrays.asList(permissions));
                            }

                            loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                                @Override
                                public void onSuccess(LoginResult loginResult) {
                                    if (hasRequiredPermissionLocally(permissions) && !subscriber.isUnsubscribed()) {
                                        LogHelper.logIfDebug(TAG, "Persmission " + permissionToLog(permissions) + " has been granted");
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
                                        // Case when app has token from other profile and tries to log in
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
                                        .compose(ResponseOrError.switchMap(waitForUserToBeUpdatedInApi(permissions)));
                            } else {
                                return Observable.just(ResponseOrError.fromData(false));
                            }
                        }
                    }));
        }
    }

    private String permissionToLog(String[] permissions) {
        final StringBuilder builder = new StringBuilder();
        for (String permission : permissions) {
            builder.append(permission).append(", ");
        }

        return builder.toString();
    }

    @NonNull
    private Func1<ResponseBody, Observable<ResponseOrError<Boolean>>> waitForUserToBeUpdatedInApi(@Nonnull final String[] requiredPermissions) {
        return responseBody -> {
            LogHelper.logIfDebug(TAG, "Waiting for user to be updated in API");
            return pusherHelperHolder.getPusherHelper().getUserUpdatedObservable()
                    .filter(user -> {
                        userPreferences.setUserOrPage(user);
                        LogHelper.logIfDebug(TAG, "Pusher event: User updated in API");

                        return hasRequiredPermissionInApi(user, requiredPermissions);
                    })
                    .map(user -> ResponseOrError.fromData(true))
                    .takeUntil(responseOrError -> responseOrError.isData() && responseOrError.data());
        };
    }

    private boolean isLoggedInToFacebook() {
        final BaseProfile user = userPreferences.getUserOrPage();
        return user != null &&
                user.getLinkedAccounts() != null &&
                user.getLinkedAccounts().getFacebook() != null
                && AccessToken.getCurrentAccessToken() != null;
    }

    public boolean hasRequiredPermissionInApi(@Nonnull BaseProfile user, @Nonnull String[] permissions) {
        if (user.getLinkedAccounts() != null &&
                user.getLinkedAccounts().getFacebook() != null) {
            final List<String> scopes = user.getLinkedAccounts().getFacebook().getScopes();

            final int permissionsToCheck = permissions.length;
            int permissionsGranted = 0;

            for (String scope : scopes) {
                for (String permission : permissions) {
                    if (scope.equals(permission)) {
                        ++permissionsGranted;
                    }
                }
            }

            return permissionsGranted >= permissionsToCheck;
        }

        return false;
    }

    public boolean hasRequiredPermissionLocally(@Nonnull String[] permissions) {
        if (AccessToken.getCurrentAccessToken() == null) {
            return false;
        }

        final Set<String> fbPermissions = AccessToken.getCurrentAccessToken().getPermissions();
        for (String permission : permissions) {
            if (!fbPermissions.contains(permission)) {
                return false;
            }
        }

        return true;
    }

    public boolean hasRequiredPermissions(@Nonnull BaseProfile profile, @Nonnull String[] permissions) {
        return hasRequiredPermissionInApi(profile, permissions) && hasRequiredPermissionLocally(permissions);
    }

    private boolean shouldRefreshToken(@Nonnull BaseProfile user) {
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
                    .setPromotionDetails(activity.getString(R.string.invite_text_fb),  promotionalCode)
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

    public Observable<List<NativeAd>> getAdsObservable(NativeAdsManager nativeAdsManager, int adsNumber) {
        final List<Observable<ResponseOrError<NativeAd>>> adsObservables = new ArrayList<>();

        final ImmutableList.Builder<NativeAd> builder = ImmutableList.builder();

        for (int i = 0; i < adsNumber; i++) {
            adsObservables.add(getAdObservable(nativeAdsManager));
        }

        return Observable.combineLatest(adsObservables, (FuncN<List<NativeAd>>) args -> {
            for (int i = 0; i < args.length; i++) {
                final ResponseOrError<NativeAd> ad = (ResponseOrError<NativeAd>) args[i];
                if (ad.isData()) {
                    builder.add(ad.data());
                }
            }

            return builder.build();
        });
    }

    @Nonnull
    public Observable<ResponseOrError<NativeAd>> getShoutDetailAdObservable() {
        return getAdObservable(getShoutDetailAdManager());
    }

    @Nonnull
    public Observable<FbAdAdapterItem> getShoutDetailAdapterItem() {
        return getShoutDetailAdObservable()
                .compose(ResponseOrError.onlySuccess())
                .map(FbAdAdapterItem::new);
    }

    @Nonnull
    public Observable<ResponseOrError<NativeAd>> getAdObservable(NativeAdsManager manager) {
        return Observable.create(new Observable.OnSubscribe<NativeAd>() {
            @Override
            public void call(Subscriber<? super NativeAd> subscriber) {
                final NativeAd ad = getAd(manager);

                if (ad == null) {
                    manager.setListener(new NativeAdsManager.Listener() {
                        @Override
                        public void onAdsLoaded() {
                            subscriber.onNext(getAd(manager));
                        }

                        @Override
                        public void onAdError(AdError adError) {
                            subscriber.onError(new Throwable(adError.getErrorMessage()));
                            LogHelper.logThrowableAndCrashlytics(TAG, "Cannot load ads", new Throwable(adError.getErrorMessage()));
                        }
                    });
                    manager.loadAds();
                } else {
                    subscriber.onNext(ad);
                }
            }
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(ResponseOrError.toResponseOrErrorObservable());
    }

    public Observable<FacebookPages> getPagesListObservable() {
        return Observable.create(new Observable.OnSubscribe<FacebookPages>() {
            @Override
            public void call(Subscriber<? super FacebookPages> subscriber) {
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/accounts",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                try {
                                    final FacebookPages facebookPages = gson.fromJson(
                                            response.getRawResponse(), FacebookPages.class);

                                    if (!subscriber.isUnsubscribed()) {
                                        subscriber.onNext(facebookPages);
                                        subscriber.onCompleted();
                                    }
                                } catch (Exception e) {
                                    if (!subscriber.isUnsubscribed()) {
                                        subscriber.onError(e);
                                        subscriber.onCompleted();
                                    }
                                }
                            }
                        }
                ).executeAndWait();
            }
        }).subscribeOn(networkScheduler);
    }

    @Nullable
    public NativeAd getAd(NativeAdsManager nativeAdsManager) {
        return nativeAdsManager.nextNativeAd();
    }

    @Nonnull
    public NativeAdsManager getListAdManager() {
        return listAdManager;
    }

    public NativeAdsManager getShoutDetailAdManager() {
        return shoutDetailAdManager;
    }

    @Nonnull
    private String getListAdId() {
        return context.getString(R.string.facebook_shout_list_ad_id);
    }

    @Nonnull
    private String getShoutDetailAdId() {
        return context.getString(R.string.facebook_shout_detail_ad_id);
    }

}
